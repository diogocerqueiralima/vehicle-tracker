#include "configuration_service.h"

#include "esp_log.h"
#include "storage/storage.h"

static const char *LOG_TAG = "configuration_service";

static const ble_uuid128_t configuration_service_uuid =
    BLE_UUID128_INIT(0x56, 0x8c, 0x41, 0xd6, 0x33, 0x0c, 0xbd, 0xba, 0x1a, 0x46, 0xaa, 0x0c, 0x34, 0x50, 0x83, 0x70);

static const ble_uuid128_t configuration_mqtt_broker_uuid =
    BLE_UUID128_INIT(0x01, 0x45, 0xc1, 0x9e, 0x02, 0xe9, 0xf5, 0x9e, 0x60, 0x4d, 0xb9, 0x2f, 0x25, 0xe7, 0x45, 0x13);

static int configure_mqtt_broker(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt *ctxt, void *arg) {
    ESP_LOGI(LOG_TAG, "Configuring MQTT Broker...");

    switch (ctxt->op) {

    case BLE_GATT_ACCESS_OP_READ_CHR: {

            ESP_LOGI(LOG_TAG, "Reading MQTT broker configuration");

            // 1. Discover the exact size of the stored value
            size_t len = 0;
            esp_err_t err = get_data_size(MQTT_BROKER_NAMESPACE, &len);
            if (err != ESP_OK) {
                ESP_LOGE(LOG_TAG, "Failed to get MQTT broker configuration size: %s", esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 2. Load the stored value into a local buffer
            char buf[len];
            err = load_data(MQTT_BROKER_NAMESPACE, buf, len);
            if (err != ESP_OK) {
                ESP_LOGE(LOG_TAG, "Failed to load MQTT broker configuration: %s", esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 3. Append the loaded value into the response mbuf
            if (os_mbuf_append(ctxt->om, buf, len) != 0) {
                return BLE_ATT_ERR_INSUFFICIENT_RES;
            }

            ESP_LOGI(LOG_TAG, "Successfully read MQTT broker configuration");
            break;
    }
    case BLE_GATT_ACCESS_OP_WRITE_CHR: {

            ESP_LOGI(LOG_TAG, "Writing MQTT broker configuration");

            // 1. Copy the full mbuf chain into a local buffer
            const uint16_t len = OS_MBUF_PKTLEN(ctxt->om);
            char buf[len];
            os_mbuf_copydata(ctxt->om, 0, len, buf);

            // 2. Persist the received value
            const esp_err_t err = save_data(MQTT_BROKER_NAMESPACE, buf, len);
            if (err != ESP_OK) {
                ESP_LOGE(LOG_TAG, "Failed to save MQTT broker configuration: %s", esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            ESP_LOGI(LOG_TAG, "Successfully saved MQTT broker configuration");
            break;
    }
    default:
        ESP_LOGI(LOG_TAG, "Unsupported GATT access operation: %d", ctxt->op);
        break;
    }

    return 0;
}

static const struct ble_gatt_chr_def characteristics[] = {
    {
        .uuid       = &configuration_mqtt_broker_uuid.u,
        .access_cb  = configure_mqtt_broker,
        .flags      = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
    },
    { 0 },
};

const struct ble_gatt_svc_def configuration_service_def = {
    .type            = BLE_GATT_SVC_TYPE_PRIMARY,
    .uuid            = &configuration_service_uuid.u,
    .characteristics = characteristics,
};