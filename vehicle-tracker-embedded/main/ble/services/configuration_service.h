#ifndef CONFIGURATION_SERVICE_H

#define CONFIGURATION_SERVICE_H

#define MQTT_BROKER_NAMESPACE                   "broker_url"
#define MQTT_KEEP_ALIVE_INTERVAL_NAMESPACE      "keep_alive"
#define MQTT_QOS_NAMESPACE                      "qos"
#define MQTT_RECONNECTION_INTERVAL_NAMESPACE    "recon_interval"
#define MQTT_TOPIC_NAMESPACE                    "topic"
#define GPS_UPDATE_INTERVAL_NAMESPACE           "gps_update"
#define GPS_TIMEOUT_NAMESPACE                   "gps_timeout"
#define CSR_NAMESPACE                           "csr"
#define CERTIFICATE_NAMESPACE                   "certificate"

#include <stdbool.h>
#include <stdint.h>
#include "host/ble_gatt.h"

/**
 * @brief Context structure for GATT characteristic access callbacks in the configuration service.
 * Contains the namespace, name, and a validation function for the configuration item being accessed,
 * allowing the same callback function to handle multiple characteristics.
 */
typedef struct {
    const char *namespace;
    const char *name;
    bool (*validate)(const char *data, uint16_t len);
} gatt_handler_context_t;

/**
 * @brief GATT service definition for the configuration service.
 * Pass this to ble_manager_register_service() before calling ble_manager_init().
 */
extern const struct ble_gatt_svc_def configuration_service_def;

#endif