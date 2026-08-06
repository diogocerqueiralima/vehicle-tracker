#include "gatt_common.h"

#include <string.h>
#include "esp_log.h"
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
            if (err != ESP_OK)
            {
                ESP_LOGE(LOG_TAG, "Failed to get %s size: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 2. Handle the case where no value is stored yet (len == 0) by returning an error to indicate that the value is not available.
            if (len == 0)
            {
                ESP_LOGE(LOG_TAG, "Stored %s value is empty", ctx->name);
                return BLE_ATT_ERR_UNLIKELY;
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
