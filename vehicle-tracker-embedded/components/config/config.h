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
    CONFIG_CSR,                     // Certificate signing request, read-only and only available while ENROLLING
    CONFIG_STATUS,                  // Status of the authentication process
    CONFIG_REVOKE,                  // Flag indicating whether the certificate should be revoked
    CONFIG_ENROLL                   // Flag indicating whether the enrollment should be started, write-only

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

/**
 *
 * @brief Sets the value for a given configuration ID.
 *
 * The value is read according to the type of the property: a pointer to an int32_t for integers, a pointer to a bool
 * for booleans and a null-terminated string for strings.
 *
 * Authentication values are handled by the security layer.
 *
 * @param id    the configuration ID to set.
 * @param value the value to set for the configuration ID.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG on an unknown ID or null value, ESP_ERR_NOT_SUPPORTED when the
 * property is not writable, or an appropriate error code on failure.
 */
esp_err_t config_set(config_id_t id, const void *value);

/**
 *
 * @brief Gets the value for a given configuration ID.
 *
 * The value is allocated according to the type of the property: an int32_t for integers, a bool for booleans and a
 * null-terminated string for strings.
 *
 * Authentication values are provided by the security layer.
 *
 * @param id    the configuration ID to get.
 * @param value the output parameter that will hold the value for the configuration ID, the caller is responsible for
 *              freeing the memory allocated for the value.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG on an unknown ID or null output, ESP_ERR_NOT_SUPPORTED when the
 * property is not readable, ESP_ERR_NVS_NOT_FOUND when no value is stored, or an appropriate error code on failure.
 */
esp_err_t config_get(config_id_t id, void **value);

#endif