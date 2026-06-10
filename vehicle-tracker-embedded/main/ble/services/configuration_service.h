#ifndef CONFIGURATION_SERVICE_H

#define CONFIGURATION_SERVICE_H

#define MQTT_BROKER_NAMESPACE "broker_url"
#define MQTT_KEEP_ALIVE_INTERVAL_NAMESPACE "keep_alive"

#include "host/ble_gatt.h"

/**
 * @brief Context structure for GATT characteristic access callbacks in the configuration service.
 * Contains the namespace and name of the configuration item being accessed, allowing the same callback function to handle multiple characteristics.
 */
typedef struct {
    const char *namespace;
    const char *name;
} gatt_handler_context_t;

/**
 * @brief GATT service definition for the configuration service.
 * Pass this to ble_manager_register_service() before calling ble_manager_init().
 */
extern const struct ble_gatt_svc_def configuration_service_def;

#endif