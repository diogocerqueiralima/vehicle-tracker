#ifndef CONFIGURATION_SERVICE_H

#define CONFIGURATION_SERVICE_H

#define MQTT_BROKER_NAMESPACE "mqtt_broker"

#include "host/ble_gatt.h"

/**
 * @brief GATT service definition for the configuration service.
 * Pass this to ble_manager_register_service() before calling ble_manager_init().
 */
extern const struct ble_gatt_svc_def configuration_service_def;

#endif