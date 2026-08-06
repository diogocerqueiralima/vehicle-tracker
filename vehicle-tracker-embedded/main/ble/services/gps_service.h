#ifndef GPS_SERVICE_H

#define GPS_SERVICE_H

#define GPS_UPDATE_INTERVAL_NAMESPACE           "gps_update"
#define GPS_TIMEOUT_NAMESPACE                   "gps_timeout"
#define GPS_MODE_NAMESPACE                      "gps_mode"

#include "host/ble_gatt.h"

/**
 * @brief GATT service definition for the GPS service.
 * Pass this to ble_manager_register_service() before calling ble_manager_init().
 */
extern const struct ble_gatt_svc_def gps_service_def;

#endif
