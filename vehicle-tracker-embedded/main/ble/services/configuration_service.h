#ifndef CONFIGURATION_SERVICE_H

#define CONFIGURATION_SERVICE_H
#include "host/ble_gatt.h"

static const ble_uuid128_t configuration_service_uuid =
    BLE_UUID128_INIT(0x56, 0x8c, 0x41, 0xd6, 0x33, 0x0c, 0xbd, 0xba, 0x1a, 0x46, 0xaa, 0x0c, 0x34, 0x50, 0x83, 0x70);

static const ble_uuid128_t configuration_mqtt_broker_uuid =
    BLE_UUID128_INIT(0x01, 0x45, 0xc1, 0x9e, 0x02, 0xe9, 0xf5, 0x9e, 0x60, 0x4d, 0xb9, 0x2f, 0x25, 0xe7, 0x45, 0x13);

/**
 * @brief GATT service definition for the configuration service, which includes characteristics for configuring the MQTT broker address.
 * This service allows clients to read and write the MQTT broker address, enabling dynamic configuration of the device's MQTT connection settings.
 */
extern const struct ble_gatt_svc_def configuration_service_def;

/**
 *
 * @brief Access callback for the MQTT broker characteristic.
 * This function is called when a client attempts to read or write the MQTT broker address.
 * It handles the logic for reading the current broker address or updating it based on the client's request.
 *
 * @param conn_handle The connection handle for the client that is accessing the characteristic.
 * @param attr_handle The attribute handle for the MQTT broker characteristic.
 * @param ctxt The GATT access context, which contains information about the operation being performed (read or write) and the data being accessed.
 * @param arg Optional argument for the callback, which can be used to pass additional information if needed.
 * @return 0 on success, or an appropriate error code if the operation fails (e.g., BLE_ATT_ERR_INSUFFICIENT_RES for insufficient resources, BLE_ATT_ERR_WRITE_NOT_PERMITTED for invalid write operations).
 */
int configure_mqtt_broker(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt *ctxt, void *arg);

#endif