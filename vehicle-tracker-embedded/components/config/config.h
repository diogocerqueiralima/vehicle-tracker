#ifndef CONFIG_H

#define CONFIG_H

#include <stddef.h>
#include <stdint.h>

#include "esp_err.h"

/**
 *
 * Every setting is read and written through this layer. Other layers, such as BLE, never access storage or the
 * security layer for a setting.
 *
 * Unconfigured settings: a setting without a default value holds nothing until a client writes it. Reading it
 * returns ESP_ERR_NOT_FOUND, which is never returned for a failure, so callers can tell the two apart.
 *
 * Writes validate the value before anything is stored, and return ESP_ERR_INVALID_ARG when it is refused. Intervals,
 * timeouts and the expiration must be greater than zero, the QoS must be 0, 1 or 2, and the broker URL must not be empty
 * and must be shorter than CONFIG_BROKER_URL_MAX_LEN.
 *
 * The broker URL is copied into a caller-provided buffer, including the null terminator. A buffer that is too small
 * yields ESP_ERR_INVALID_SIZE. PEM values are returned as allocated strings that the caller frees.
 */

/** Maximum size of the broker URL in bytes, including the null terminator. */
#define CONFIG_BROKER_URL_MAX_LEN   256

/** Default values written on boot for the settings that hold no value yet. */
#define CONFIG_DEFAULT_KEEP_ALIVE       60
#define CONFIG_DEFAULT_QOS              0
#define CONFIG_DEFAULT_RECON_INTERVAL   30
#define CONFIG_DEFAULT_GPS_UPDATE       60
#define CONFIG_DEFAULT_GPS_TIMEOUT      60
#define CONFIG_DEFAULT_GPS_MODE         CONFIG_GPS_MODE_UE_BASED
#define CONFIG_DEFAULT_EXPIRATION       31536000

/** Fix strategy of the GPS. */
typedef enum
{
    CONFIG_GPS_MODE_STANDALONE,
    CONFIG_GPS_MODE_UE_BASED,
    CONFIG_GPS_MODE_UE_ASSISTED
} config_gps_mode_t;

/** Status of the device certificate. */
typedef enum
{
    CONFIG_CERT_STATUS_VALID,
    CONFIG_CERT_STATUS_EXPIRED,
    CONFIG_CERT_STATUS_PENDING,
    CONFIG_CERT_STATUS_REVOKED
} config_cert_status_t;

/**
 *
 * @brief Initializes the config layer. Must be called after storage_init() and security_init().
 *
 * Writes the documented default of every setting that holds no value yet. A setting that already holds a value is
 * never overwritten. Settings without a default stay unconfigured.
 *
 * @return ESP_OK on success, or the error of the first default that cannot be read or written.
 */
esp_err_t config_init();

/**
 *
 * @brief Reads the URL of the MQTT broker the device connects to.
 *
 * @param out_value the buffer that receives the null-terminated URL. Must not be NULL.
 * @param max_len the size of the buffer in bytes. CONFIG_BROKER_URL_MAX_LEN is always enough.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the setting is unconfigured, ESP_ERR_INVALID_ARG on a null argument,
 * ESP_ERR_INVALID_SIZE if the buffer is too small, or an appropriate error code on failure.
 */
esp_err_t config_get_broker_url(char *out_value, size_t max_len);

/**
 *
 * @brief Sets the URL of the MQTT broker the device connects to. Must not be empty and must be shorter than
 * CONFIG_BROKER_URL_MAX_LEN.
 *
 * @param value the null-terminated URL. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_broker_url(const char *value);

/**
 *
 * @brief Reads the keep-alive interval of the MQTT connection, in seconds.
 *
 * @param out_value receives the value. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the setting is unconfigured, ESP_ERR_INVALID_ARG on a null argument,
 * or an appropriate error code on failure.
 */
esp_err_t config_get_keep_alive(uint32_t *out_value);

/**
 *
 * @brief Sets the keep-alive interval of the MQTT connection, in seconds. Must be greater than zero.
 *
 * @param value the new value.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_keep_alive(uint32_t value);

/**
 *
 * @brief Reads the Quality of Service level of the MQTT messages.
 *
 * @param out_value receives the value. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the setting is unconfigured, ESP_ERR_INVALID_ARG on a null argument,
 * or an appropriate error code on failure.
 */
esp_err_t config_get_qos(uint8_t *out_value);

/**
 *
 * @brief Sets the Quality of Service level of the MQTT messages. Accepts 0, 1 or 2.
 *
 * @param value the new value.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_qos(uint8_t value);

/**
 *
 * @brief Reads the interval between reconnection attempts to the MQTT broker, in seconds.
 *
 * @param out_value receives the value. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the setting is unconfigured, ESP_ERR_INVALID_ARG on a null argument,
 * or an appropriate error code on failure.
 */
esp_err_t config_get_recon_interval(uint32_t *out_value);

/**
 *
 * @brief Sets the interval between reconnection attempts to the MQTT broker, in seconds. Must be greater than zero.
 *
 * @param value the new value.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_recon_interval(uint32_t value);

/**
 *
 * @brief Reads how often the device updates its GPS location while moving, in seconds.
 *
 * @param out_value receives the value. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the setting is unconfigured, ESP_ERR_INVALID_ARG on a null argument,
 * or an appropriate error code on failure.
 */
esp_err_t config_get_gps_update(uint32_t *out_value);

/**
 *
 * @brief Sets how often the device updates its GPS location while moving, in seconds. Must be greater than zero.
 *
 * @param value the new value.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_gps_update(uint32_t value);

/**
 *
 * @brief Reads the maximum time the device waits for a GPS fix, in seconds.
 *
 * @param out_value receives the value. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the setting is unconfigured, ESP_ERR_INVALID_ARG on a null argument,
 * or an appropriate error code on failure.
 */
esp_err_t config_get_gps_timeout(uint32_t *out_value);

/**
 *
 * @brief Sets the maximum time the device waits for a GPS fix, in seconds. Must be greater than zero.
 *
 * @param value the new value.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_gps_timeout(uint32_t value);

/**
 *
 * @brief Reads the fix strategy of the GPS.
 *
 * @param out_value receives the value. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the setting is unconfigured, ESP_ERR_INVALID_ARG on a null argument,
 * or an appropriate error code on failure.
 */
esp_err_t config_get_gps_mode(config_gps_mode_t *out_value);

/**
 *
 * @brief Sets the fix strategy of the GPS. Must be one of config_gps_mode_t.
 *
 * @param value the new value.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_gps_mode(config_gps_mode_t value);

/**
 *
 * @brief Reads the expiration time, in seconds, used when generating the certificate.
 *
 * @param out_value receives the value. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the setting is unconfigured, ESP_ERR_INVALID_ARG on a null argument,
 * or an appropriate error code on failure.
 */
esp_err_t config_get_expiration(uint32_t *out_value);

/**
 *
 * @brief Sets the expiration time, in seconds, used when generating the certificate. Must be greater than zero.
 *
 * @param value the new value.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_expiration(uint32_t value);

/**
 *
 * @brief Reads the installed certificate as a PEM string.
 *
 * @param err receives ESP_OK on success, ESP_ERR_NOT_FOUND when no certificate is installed, or an appropriate error code
 * on failure. May be NULL when the caller does not need the error.
 * @return the certificate as a null-terminated string in PEM format, or NULL when @p err is not ESP_OK. The caller is
 * responsible for freeing the returned string.
 */
char *config_get_certificate(esp_err_t *err);

/**
 *
 * @brief Installs the certificate issued for the stored CSR.
 *
 * The certificate is validated, then stored, then the stored CSR is deleted. A value that is not a valid PEM
 * certificate is refused and nothing changes.
 *
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_certificate(const char *value);

/**
 *
 * @brief Reads the CA certificate as a PEM string.
 *
 * @param err receives ESP_OK on success, ESP_ERR_NOT_FOUND when no CA certificate is set, or an appropriate error code
 * on failure. May be NULL when the caller does not need the error.
 * @return the CA certificate as a null-terminated string in PEM format, or NULL when @p err is not ESP_OK. The caller is
 * responsible for freeing the returned string.
 */
char *config_get_ca(esp_err_t *err);

/**
 *
 * @brief Replaces the CA certificate. A value that is not a valid PEM certificate, or that holds more than one
 * certificate, is refused.
 *
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is refused, or an appropriate error code on failure.
 */
esp_err_t config_set_ca(const char *value);

/**
 *
 * @brief Reads the CSR as a PEM string. Read-only: there is no setter.
 *
 * Returns the stored request. When none is stored and no certificate is installed, the CSR is created first. The same
 * request is returned on every read until a certificate is installed, so a client can read it in chunks that all come
 * from the same request.
 *
 * @param err receives ESP_OK on success, ESP_ERR_INVALID_STATE when a certificate is installed and no request is stored
 * (the credentials must be revoked first), or an appropriate error code on failure. May be NULL when the caller does not
 * need the error.
 * @return the CSR as a null-terminated string in PEM format, or NULL when @p err is not ESP_OK. The caller is
 * responsible for freeing the returned string.
 */
char *config_get_csr(esp_err_t *err);

/**
 *
 * @brief Reads the status of the certificate. Read-only: there is no setter.
 *
 * With a certificate installed the status is valid, or expired once the end of its validity has passed. The clock
 * must be synchronized before this function is called, because the expiration is judged against it. Without a
 * certificate the status is pending, or revoked when the credentials were revoked and no new request was created yet.
 *
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t config_get_status(config_cert_status_t *out_value);

/**
 *
 * @brief Revokes the device credentials. This is the `revoke` command, so nothing is stored.
 *
 * Deletes the installed certificate, then the stored CSR, then the private key. The CA certificate stays. Missing
 * items are not an error.
 *
 * @param revoke 1 revokes the credentials, 0 does nothing.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when the value is neither 0 nor 1, or an appropriate error code on failure.
 */
esp_err_t config_command_revoke(uint8_t revoke);

#endif
