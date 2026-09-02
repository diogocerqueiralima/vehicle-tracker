#include "gatt_common.h"

#include <string.h>
#include "esp_log.h"
#include "nvs.h"
#include "storage/storage.h"

static const char* LOG_TAG = "gatt_common";

int gatt_common_access_cb(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt* ctxt, void* arg)
{
    (void)conn_handle;
    (void)attr_handle;

    const gatt_handler_context_t* ctx = (gatt_handler_context_t*)arg;

    switch (ctxt->op)
    {
    case BLE_GATT_ACCESS_OP_READ_CHR:
        {
            ESP_LOGI(LOG_TAG, "Reading %s configuration", ctx->name);

            // 1. Get the size of the stored value to determine how much data to read.
            size_t len = 0;
            esp_err_t err = get_data_size(ctx->namespace, &len);

            // 1.1 A missing namespace/key is the expected state of a setting that was never written,
            // so report it as such instead of as a failure.
            if (err == ESP_ERR_NVS_NOT_FOUND)
            {
                ESP_LOGW(LOG_TAG, "%s is not configured yet", ctx->name);
                return GATT_ATT_ERR_NOT_CONFIGURED;
            }

            if (err != ESP_OK)
            {
                ESP_LOGE(LOG_TAG, "Failed to get %s size: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 2. Handle the case where no value is stored yet (len == 0) the same way as a missing key.
            if (len == 0)
            {
                ESP_LOGW(LOG_TAG, "Stored %s value is empty", ctx->name);
                return GATT_ATT_ERR_NOT_CONFIGURED;
            }

            // 3. Read the stored value.
            char buf[len];
            err = load_data(ctx->namespace, buf, len);
            if (err != ESP_OK)
            {
                ESP_LOGE(LOG_TAG, "Failed to load %s: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 4. Append the value to the response buffer to be sent back to the client.
            if (os_mbuf_append(ctxt->om, buf, len) != 0)
            {
                return BLE_ATT_ERR_INSUFFICIENT_RES;
            }

            ESP_LOGI(LOG_TAG, "Successfully read %s", ctx->name);
            break;
        }
    case BLE_GATT_ACCESS_OP_WRITE_CHR:
        {
            ESP_LOGI(LOG_TAG, "Writing %s configuration", ctx->name);

            // 5. Check if the length of the incoming data is valid
            const uint16_t len = OS_MBUF_PKTLEN(ctxt->om);
            if (len <= 0)
            {
                return BLE_ATT_ERR_INVALID_ATTR_VALUE_LEN;
            }

            // 6. Copy the incoming data from the mbuf into a local buffer for validation and storage.
            char buf[len];
            os_mbuf_copydata(ctxt->om, 0, len, buf);

            // 7. Validate the value using the provided validation function, if any.
            if (ctx->validate != NULL && !ctx->validate(buf, len))
            {
                ESP_LOGE(LOG_TAG, "Invalid value for %s:", ctx->name);
                ESP_LOG_BUFFER_HEX_LEVEL(LOG_TAG, buf, len, ESP_LOG_ERROR);
                return BLE_ATT_ERR_VALUE_NOT_ALLOWED;
            }

            // 8. Save the value to NVS for persistent storage.
            const esp_err_t err = save_data(ctx->namespace, buf, len);
            if (err != ESP_OK)
            {
                ESP_LOGE(LOG_TAG, "Failed to save %s: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            ESP_LOGI(LOG_TAG, "Successfully saved %s", ctx->name);
            break;
        }
    default:
        break;
    }

    return 0;
}

esp_err_t gatt_common_seed_defaults(const struct ble_gatt_svc_def* svc_def)
{
    if (svc_def == nullptr || svc_def->characteristics == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 1. Walk the characteristics of the service up to the {0} terminator entry.
    for (const struct ble_gatt_chr_def* chr = svc_def->characteristics; chr->uuid != nullptr; chr++)
    {
        const gatt_handler_context_t* ctx = chr->arg;

        // 2. Skip characteristics that have no context or no documented default value.
        if (ctx == nullptr || ctx->default_value == nullptr || ctx->default_len == 0)
        {
            continue;
        }

        // 3. Only a characteristic that was never written gets its default: ESP_ERR_NVS_NOT_FOUND means
        // the namespace/key does not exist yet, any other result means the value is already configured
        // (or unreadable) and must be left untouched.
        size_t len = 0;
        const esp_err_t size_err = get_data_size(ctx->namespace, &len);
        if (size_err != ESP_ERR_NVS_NOT_FOUND)
        {
            continue;
        }

        // 4. Persist the default so subsequent reads of the characteristic succeed.
        const esp_err_t err = save_data(ctx->namespace, ctx->default_value, ctx->default_len);
        if (err != ESP_OK)
        {
            ESP_LOGE(LOG_TAG, "Failed to seed default %s: %s", ctx->name, esp_err_to_name(err));
            return err;
        }

        ESP_LOGI(LOG_TAG, "Seeded default %s", ctx->name);
    }

    return ESP_OK;
}
