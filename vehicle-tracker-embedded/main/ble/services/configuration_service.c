#include "configuration_service.h"

#include "esp_log.h"
#include "storage/storage.h"

static const char *LOG_TAG = "configuration_service";

static const ble_uuid128_t configuration_service_uuid =
    BLE_UUID128_INIT(0x56, 0x8c, 0x41, 0xd6, 0x33, 0x0c, 0xbd, 0xba, 0x1a, 0x46, 0xaa, 0x0c, 0x34, 0x50, 0x83, 0x70);

static const ble_uuid128_t configuration_mqtt_broker_uuid =
    BLE_UUID128_INIT(0x01, 0x45, 0xc1, 0x9e, 0x02, 0xe9, 0xf5, 0x9e, 0x60, 0x4d, 0xb9, 0x2f, 0x25, 0xe7, 0x45, 0x13);

static const ble_uuid128_t configuration_mqtt_keep_alive_uuid =
    BLE_UUID128_INIT(0x8f, 0x3a, 0x7d, 0x51, 0x19, 0xc4, 0x4e, 0xb2, 0x82, 0x1f, 0xe6, 0x9a, 0x3c, 0xd2, 0x78, 0x54);

static int configure_gatt_characteristic(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt *ctxt, void *arg) {

    const gatt_handler_context_t *ctx = (gatt_handler_context_t *) arg;

    switch (ctxt->op) {
    case BLE_GATT_ACCESS_OP_READ_CHR: {

            ESP_LOGI(LOG_TAG, "Reading %s configuration", ctx->name);

            size_t len = 0;
            esp_err_t err = get_data_size(ctx->namespace, &len);
            if (err != ESP_OK) {
                ESP_LOGE(LOG_TAG, "Failed to get %s size: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            char buf[len];
            err = load_data(ctx->namespace, buf, len);
            if (err != ESP_OK) {
                ESP_LOGE(LOG_TAG, "Failed to load %s: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            if (os_mbuf_append(ctxt->om, buf, len) != 0) {
                return BLE_ATT_ERR_INSUFFICIENT_RES;
            }

            ESP_LOGI(LOG_TAG, "Successfully read %s", ctx->name);
            break;
    }
    case BLE_GATT_ACCESS_OP_WRITE_CHR: {

            ESP_LOGI(LOG_TAG, "Writing %s configuration", ctx->name);

            const uint16_t len = OS_MBUF_PKTLEN(ctxt->om);
            char buf[len];
            os_mbuf_copydata(ctxt->om, 0, len, buf);

            const esp_err_t err = save_data(ctx->namespace, buf, len);
            if (err != ESP_OK) {
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

static const struct ble_gatt_chr_def characteristics[] = {
    {
        .uuid       = &configuration_mqtt_broker_uuid.u,
        .access_cb  = configure_gatt_characteristic,
        .flags      = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg        = &(gatt_handler_context_t) {
            .namespace  = MQTT_BROKER_NAMESPACE,
            .name       = "MQTT broker"
        },
    },
    {
        .uuid       = &configuration_mqtt_keep_alive_uuid.u,
        .access_cb  = configure_gatt_characteristic,
        .flags      = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg        = &(gatt_handler_context_t) {
            .namespace  = MQTT_KEEP_ALIVE_INTERVAL_NAMESPACE,
            .name       = "MQTT keep alive interval"
        },
    },
    { 0 },
};

const struct ble_gatt_svc_def configuration_service_def = {
    .type            = BLE_GATT_SVC_TYPE_PRIMARY,
    .uuid            = &configuration_service_uuid.u,
    .characteristics = characteristics,
};