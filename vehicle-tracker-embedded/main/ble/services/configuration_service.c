#include "configuration_service.h"

#include "esp_log.h"

static const char *LOG_TAG = "configuration_service";

static const ble_uuid128_t configuration_service_uuid =
    BLE_UUID128_INIT(0x56, 0x8c, 0x41, 0xd6, 0x33, 0x0c, 0xbd, 0xba, 0x1a, 0x46, 0xaa, 0x0c, 0x34, 0x50, 0x83, 0x70);

static const ble_uuid128_t configuration_mqtt_broker_uuid =
    BLE_UUID128_INIT(0x01, 0x45, 0xc1, 0x9e, 0x02, 0xe9, 0xf5, 0x9e, 0x60, 0x4d, 0xb9, 0x2f, 0x25, 0xe7, 0x45, 0x13);

static int configure_mqtt_broker(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt *ctxt, void *arg) {
    ESP_LOGI(LOG_TAG, "Configuring MQTT Broker...");
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