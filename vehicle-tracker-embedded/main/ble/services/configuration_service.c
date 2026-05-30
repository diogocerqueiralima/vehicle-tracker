#include "configuration_service.h"

const struct ble_gatt_svc_def configuration_service_def = {
    .type = BLE_GATT_SVC_TYPE_PRIMARY,
    .uuid = &configuration_service_uuid.u,
    .characteristics = (struct ble_gatt_chr_def[]) {
            { // MQTT Broker characteristic
                .uuid = &configuration_mqtt_broker_uuid.u,
                .access_cb = configure_mqtt_broker,
                .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
                .val_handle = NULL
            },
            { 0 }, // No more characteristics
        }
};

int configure_mqtt_broker(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt *ctxt, void *arg) {
    return 0;
}