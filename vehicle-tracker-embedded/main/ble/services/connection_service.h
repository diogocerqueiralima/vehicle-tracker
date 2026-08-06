#ifndef CONNECTION_SERVICE_H

#define CONNECTION_SERVICE_H

#define MQTT_BROKER_NAMESPACE                   "broker_url"
#define MQTT_KEEP_ALIVE_INTERVAL_NAMESPACE      "keep_alive"
#define MQTT_QOS_NAMESPACE                      "qos"
#define MQTT_RECONNECTION_INTERVAL_NAMESPACE    "recon_interval"

#include "host/ble_gatt.h"

/**
 * @brief GATT service definition for the connection service.
 * Pass this to ble_manager_register_service() before calling ble_manager_init().
 */
extern const struct ble_gatt_svc_def connection_service_def;

#endif
