#ifndef CONFIG_H

#define CONFIG_H
#include "esp_err.h"

typedef enum {

    /** Connection **/

    CONFIG_BROKER_URL,              // URL of the MQTT broker to connect to
    CONFIG_KEEP_ALIVE,              // Keep alive interval for the MQTT connection
    CONFIG_QOS,                     // Quality of Service level for MQTT messages
    CONFIG_RECONNECT_INTERVAL,      // Interval for attempting to reconnect to the MQTT broker

    /** GPS **/

    CONFIG_GPS_UPDATE_INTERVAL,     // Interval for updating GPS data
    CONFIG_GPS_TIMEOUT,             // Timeout for GPS fix acquisition
    CONFIG_GPS_MODE,                // Mode of GPS operation (e.g., standalone, ue-based, ue-assisted)

    /** Authentication **/

    CONFIG_CERTIFICATE,             // Client certificate for secure connections
    CONFIG_CERTIFICATE_AUTHORITY,   // Certificate authority for validating server certificates
    CONFIG_CSR,                     // Certificate signing request for obtaining a certificate
    CONFIG_STATUS,                  // Status of the authentication process
    CONFIG_REVOKE                   // Flag indicating whether the certificate should be revoked

} config_id_t;

/**
 *
 * @brief Initializes the configuration system, preparing it for read/write operations.
 *
 * Every configuration property with a default value set is initialized to that value if it is not already set.
 *
 * The storage and security systems must be already initialized before calling this function.
 *
 * @return ESP_OK on successful initialization, or an appropriate error code on failure.
 */
esp_err_t config_init();

#endif