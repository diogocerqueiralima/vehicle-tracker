#include "config.h"

#include <stdlib.h>
#include <string.h>

#include "esp_log.h"
#include "nvs.h"
#include "security.h"
#include "storage.h"

#define NS_CONNECTION   "connection"
#define NS_GPS          "gps"
#define NS_AUTH         "auth"

static const char *TAG = "[CONFIG]";

static const storage_ctx_t broker_url_ctx    = STORAGE_RW(NS_CONNECTION, "broker_url");
static const storage_ctx_t keep_alive_ctx    = STORAGE_RW(NS_CONNECTION, "keep_alive");
static const storage_ctx_t qos_ctx           = STORAGE_RW(NS_CONNECTION, "qos");
static const storage_ctx_t recon_interval_ctx = STORAGE_RW(NS_CONNECTION, "recon_interval");

static const storage_ctx_t gps_update_ctx    = STORAGE_RW(NS_GPS, "gps_update");
static const storage_ctx_t gps_timeout_ctx   = STORAGE_RW(NS_GPS, "gps_timeout");
static const storage_ctx_t gps_mode_ctx      = STORAGE_RW(NS_GPS, "gps_mode");

static const storage_ctx_t expiration_ctx    = STORAGE_RW(NS_AUTH, "expiration");

// Set when the credentials are revoked and cleared when a new CSR is created. It is device state, not the revoke command.
static const storage_ctx_t revoked_ctx       = STORAGE_RW(NS_AUTH, "revoked");

static const char *const GPS_MODE_NAMES[] = {
    [CONFIG_GPS_MODE_STANDALONE]  = "standalone",
    [CONFIG_GPS_MODE_UE_BASED]    = "ue-based",
    [CONFIG_GPS_MODE_UE_ASSISTED] = "ue-assisted"
};

#define GPS_MODE_COUNT  (sizeof(GPS_MODE_NAMES) / sizeof(GPS_MODE_NAMES[0]))
#define GPS_MODE_MAX_LEN 16 // Fits the longest mode name and the null terminator.

/**
 *
 * @brief Maps the storage errors to the ones the config layer reports. A value that is not stored becomes
 * ESP_ERR_NOT_FOUND, which only means "unconfigured", and a buffer that is too small becomes ESP_ERR_INVALID_SIZE.
 *
 * @param err the error returned by the storage layer.
 * @return the error to report to the caller.
 */
static esp_err_t map_error(const esp_err_t err) {
    if (err == ESP_ERR_NVS_NOT_FOUND) {
        return ESP_ERR_NOT_FOUND;
    }

    if (err == ESP_ERR_NVS_INVALID_LENGTH) {
        return ESP_ERR_INVALID_SIZE;
    }

    return err;
}

static esp_err_t read_u32(const storage_ctx_t *ctx, uint32_t *out_value) {

    if (out_value == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    const esp_err_t err = map_error(storage_get_u32(ctx, out_value));
    if (err != ESP_OK && err != ESP_ERR_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to read %s: %s", ctx->key, esp_err_to_name(err));
    }

    return err;
}

static esp_err_t write_u32(const storage_ctx_t *ctx, const uint32_t value) {

    // A zero interval or timeout would make the device spin or never wait, so it is refused
    if (value == 0) {
        return ESP_ERR_INVALID_ARG;
    }

    const esp_err_t err = storage_set_u32(ctx, value);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to write %s: %s", ctx->key, esp_err_to_name(err));
    }

    return err;
}

static esp_err_t read_str(const storage_ctx_t *ctx, char *out_value, const size_t max_len) {

    if (out_value == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    const esp_err_t err = map_error(storage_get_str(ctx, out_value, max_len));
    if (err != ESP_OK && err != ESP_ERR_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to read %s: %s", ctx->key, esp_err_to_name(err));
    }

    return err;
}

/**
 *
 * @brief Writes a default for a setting that holds no value yet. A value that is already stored is left untouched.
 *
 * @param ctx the context of the setting.
 * @param value the default to write.
 * @return ESP_OK when the setting holds a value afterwards, or the error that prevented it.
 */
static esp_err_t default_u32(const storage_ctx_t *ctx, const uint32_t value) {

    uint32_t current;
    esp_err_t err = storage_get_u32(ctx, &current);

    if (err == ESP_OK) {
        return ESP_OK;
    }

    if (err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to read %s: %s", ctx->key, esp_err_to_name(err));
        return err;
    }

    err = storage_set_u32(ctx, value);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to write default of %s: %s", ctx->key, esp_err_to_name(err));
    }

    return err;
}

/**
 *
 * @brief Makes sure the CSR is stored, creating it when it is not and no certificate is installed.
 *
 * A stored CSR is always used first, so an interrupted install, which leaves both a certificate and a CSR, still
 * returns the request the certificate was issued for.
 *
 * @param out_len receives the size of the stored CSR, including the null terminator.
 * @return ESP_OK on success, ESP_ERR_INVALID_STATE when a certificate is installed and no CSR is stored, or an
 * appropriate error code on failure.
 */
static esp_err_t ensure_csr(size_t *out_len) {

    // 1. Use the stored CSR when there is one
    esp_err_t err = security_get_csr_size(out_len);
    if (err != ESP_ERR_NOT_FOUND) {
        return err;
    }

    // 2. Create one. The security layer refuses when a certificate is installed, so a new key never replaces the one
    // the certificate depends on.
    err = security_create_csr();
    if (err != ESP_OK) {
        return err;
    }

    // 3. A new request means the credentials are no longer revoked
    err = storage_erase(&revoked_ctx);
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to clear the revoked marker: %s", esp_err_to_name(err));
        return err;
    }

    return security_get_csr_size(out_len);
}

/**
 *
 * @brief Reads a PEM value into a newly allocated string, reporting why it could not be read.
 *
 * @param get_size the security function that reports the size of the value.
 * @param read the security function that copies the value into a buffer.
 * @param err receives ESP_OK on success, or the error that prevented the read. May be NULL.
 * @return the null-terminated PEM string, which the caller must free, or NULL when @p err is not ESP_OK.
 */
static char *read_pem(esp_err_t (*get_size)(size_t *), esp_err_t (*read)(char *, size_t), esp_err_t *err) {

    // 1. Get the size of the value, so the buffer fits it
    size_t size = 0;
    esp_err_t result = get_size(&size);
    if (result != ESP_OK) {
        if (err != nullptr) {
            *err = result;
        }
        return nullptr;
    }

    // 2. Allocate the buffer
    char *pem = malloc(size);
    if (pem == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate memory for PEM");
        if (err != nullptr) {
            *err = ESP_ERR_NO_MEM;
        }
        return nullptr;
    }

    // 3. Read the value into the buffer
    result = read(pem, size);
    if (result != ESP_OK) {
        free(pem);
        pem = nullptr;
    }

    if (err != nullptr) {
        *err = result;
    }

    return pem;
}

esp_err_t config_init() {

    ESP_LOGI(TAG, "Initializing config");

    // 1. Write the default of every setting that holds no value yet. Existing values are never overwritten.
    esp_err_t err = default_u32(&keep_alive_ctx, CONFIG_DEFAULT_KEEP_ALIVE);
    if (err != ESP_OK) {
        return err;
    }

    // 1.1 The QoS is a small integer, so it is stored as a single byte
    uint8_t qos;
    err = storage_get_u8(&qos_ctx, &qos);
    if (err == ESP_ERR_NVS_NOT_FOUND) {
        err = storage_set_u8(&qos_ctx, CONFIG_DEFAULT_QOS);
    }
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to initialize qos: %s", esp_err_to_name(err));
        return err;
    }

    err = default_u32(&recon_interval_ctx, CONFIG_DEFAULT_RECON_INTERVAL);
    if (err != ESP_OK) {
        return err;
    }

    err = default_u32(&gps_update_ctx, CONFIG_DEFAULT_GPS_UPDATE);
    if (err != ESP_OK) {
        return err;
    }

    err = default_u32(&gps_timeout_ctx, CONFIG_DEFAULT_GPS_TIMEOUT);
    if (err != ESP_OK) {
        return err;
    }

    err = default_u32(&expiration_ctx, CONFIG_DEFAULT_EXPIRATION);
    if (err != ESP_OK) {
        return err;
    }

    // 1.2 The GPS mode is stored by name
    char mode[GPS_MODE_MAX_LEN];
    err = storage_get_str(&gps_mode_ctx, mode, sizeof(mode));
    if (err == ESP_ERR_NVS_NOT_FOUND) {
        err = storage_set_str(&gps_mode_ctx, GPS_MODE_NAMES[CONFIG_DEFAULT_GPS_MODE]);
    }
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to initialize gps_mode: %s", esp_err_to_name(err));
        return err;
    }

    ESP_LOGI(TAG, "Config initialized");
    return ESP_OK;
}

// --- Connection ---

esp_err_t config_get_broker_url(char *out_value, const size_t max_len) {
    return read_str(&broker_url_ctx, out_value, max_len);
}

esp_err_t config_set_broker_url(const char *value) {

    if (value == nullptr || value[0] == '\0' || strlen(value) >= CONFIG_BROKER_URL_MAX_LEN) {
        return ESP_ERR_INVALID_ARG;
    }

    const esp_err_t err = storage_set_str(&broker_url_ctx, value);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to write broker_url: %s", esp_err_to_name(err));
    }

    return err;
}

esp_err_t config_get_keep_alive(uint32_t *out_value) {
    return read_u32(&keep_alive_ctx, out_value);
}

esp_err_t config_set_keep_alive(const uint32_t value) {
    return write_u32(&keep_alive_ctx, value);
}

esp_err_t config_get_qos(uint8_t *out_value) {

    if (out_value == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    const esp_err_t err = map_error(storage_get_u8(&qos_ctx, out_value));
    if (err != ESP_OK && err != ESP_ERR_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to read qos: %s", esp_err_to_name(err));
    }

    return err;
}

esp_err_t config_set_qos(const uint8_t value) {

    if (value > 2) {
        return ESP_ERR_INVALID_ARG;
    }

    const esp_err_t err = storage_set_u8(&qos_ctx, value);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to write qos: %s", esp_err_to_name(err));
    }

    return err;
}

esp_err_t config_get_recon_interval(uint32_t *out_value) {
    return read_u32(&recon_interval_ctx, out_value);
}

esp_err_t config_set_recon_interval(const uint32_t value) {
    return write_u32(&recon_interval_ctx, value);
}

// --- GPS ---

esp_err_t config_get_gps_update(uint32_t *out_value) {
    return read_u32(&gps_update_ctx, out_value);
}

esp_err_t config_set_gps_update(const uint32_t value) {
    return write_u32(&gps_update_ctx, value);
}

esp_err_t config_get_gps_timeout(uint32_t *out_value) {
    return read_u32(&gps_timeout_ctx, out_value);
}

esp_err_t config_set_gps_timeout(const uint32_t value) {
    return write_u32(&gps_timeout_ctx, value);
}

esp_err_t config_get_gps_mode(config_gps_mode_t *out_value) {

    if (out_value == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 1. Read the stored mode name
    char name[GPS_MODE_MAX_LEN];
    const esp_err_t err = read_str(&gps_mode_ctx, name, sizeof(name));
    if (err != ESP_OK) {
        return err;
    }

    // 2. Map the name back to the mode
    for (size_t i = 0; i < GPS_MODE_COUNT; i++) {
        if (strcmp(name, GPS_MODE_NAMES[i]) == 0) {
            *out_value = (config_gps_mode_t) i;
            return ESP_OK;
        }
    }

    // 2.1 A stored name that is no mode means the value is corrupt, which is a failure and not an unconfigured setting
    ESP_LOGE(TAG, "Stored gps_mode is not valid: %s", name);
    return ESP_ERR_INVALID_STATE;
}

esp_err_t config_set_gps_mode(const config_gps_mode_t value) {

    if ((size_t) value >= GPS_MODE_COUNT) {
        return ESP_ERR_INVALID_ARG;
    }

    const esp_err_t err = storage_set_str(&gps_mode_ctx, GPS_MODE_NAMES[value]);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to write gps_mode: %s", esp_err_to_name(err));
    }

    return err;
}

// --- Authentication ---

esp_err_t config_get_expiration(uint32_t *out_value) {
    return read_u32(&expiration_ctx, out_value);
}

esp_err_t config_set_expiration(const uint32_t value) {
    return write_u32(&expiration_ctx, value);
}

char *config_get_certificate(esp_err_t *err) {
    return read_pem(security_get_certificate_size, security_read_certificate, err);
}

esp_err_t config_set_certificate(const char *value) {
    return security_install_certificate(value);
}

char *config_get_ca(esp_err_t *err) {
    return read_pem(security_get_ca_certificate_size, security_read_ca_certificate, err);
}

esp_err_t config_set_ca(const char *value) {
    return security_set_ca_certificate(value);
}

char *config_get_csr(esp_err_t *err) {

    // 1. Make sure a CSR is stored, creating it when needed
    size_t size;
    const esp_err_t result = ensure_csr(&size);
    if (result != ESP_OK) {
        if (err != nullptr) {
            *err = result;
        }
        return nullptr;
    }

    // 2. Read it
    return read_pem(security_get_csr_size, security_read_csr, err);
}

esp_err_t config_get_status(config_cert_status_t *out_value) {

    if (out_value == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 1. A stored request means the device waits for a certificate, whatever an interrupted revoke or install left behind
    size_t csr_size;
    esp_err_t err = security_get_csr_size(&csr_size);
    if (err == ESP_OK) {
        *out_value = CONFIG_CERT_STATUS_PENDING;
        return ESP_OK;
    }

    if (err != ESP_ERR_NOT_FOUND) {
        return err;
    }

    // 2. A certificate is installed: it is valid until it expires
    bool expired;
    err = security_certificate_is_expired(&expired);
    if (err == ESP_OK) {
        *out_value = expired ? CONFIG_CERT_STATUS_EXPIRED : CONFIG_CERT_STATUS_VALID;
        return ESP_OK;
    }

    if (err != ESP_ERR_NOT_FOUND) {
        return err;
    }

    // 3. No certificate and no request: the credentials are revoked until a new request is created, and pending after that
    uint8_t revoked;
    err = storage_get_u8(&revoked_ctx, &revoked);
    if (err == ESP_ERR_NVS_NOT_FOUND) {
        *out_value = CONFIG_CERT_STATUS_PENDING;
        return ESP_OK;
    }

    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to read the revoked marker: %s", esp_err_to_name(err));
        return err;
    }

    *out_value = CONFIG_CERT_STATUS_REVOKED;
    return ESP_OK;
}

esp_err_t config_command_revoke(const uint8_t revoke) {

    // 1. Only 0 and 1 are commands
    if (revoke > 1) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Writing 0 does nothing
    if (revoke == 0) {
        return ESP_OK;
    }

    // 3. Delete the certificate, the CSR and the private key, in that order
    esp_err_t err = security_revoke();
    if (err != ESP_OK) {
        return err;
    }

    // 4. Remember that the credentials were revoked, so the status tells it apart from a device that never had any
    err = storage_set_u8(&revoked_ctx, 1);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to store the revoked marker: %s", esp_err_to_name(err));
        return err;
    }

    return ESP_OK;
}
