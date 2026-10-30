#include <stdlib.h>
#include <string.h>

#include "config.h"
#include "esp_log.h"
#include "nvs.h"
#include "security.h"
#include "storage.h"

#define CONFIG_NAMESPACE_CONNECTION "connection"
#define CONFIG_NAMESPACE_GPS "gps"

#define TAG "config"

/**
 * Represents the category a configuration item belongs to, which decides where its value comes from.
 */
typedef enum {

    CONFIG_CATEGORY_GPS,
    CONFIG_CATEGORY_AUTHENTICATION,
    CONFIG_CATEGORY_CONNECTION

} config_category_t;

/**
 * Represents the type of value held by a configuration item.
 */
typedef enum {

    CONFIG_TYPE_INT,
    CONFIG_TYPE_BOOL,
    CONFIG_TYPE_STRING

} config_type_t;

/**
 * Represents a configuration item with its associated properties.
 */
typedef struct {

    config_category_t category;

    /** Where the value is stored. Both are NULL when the value is owned by the security layer. **/
    char *namespace;
    char *key;

    config_type_t type;

    bool writable;
    bool readable;

    /** Whether the property has a default value, which is needed because an integer default can be zero. **/
    bool has_default;

    /** Only used by CONFIG_TYPE_INT and CONFIG_TYPE_STRING, and only when has_default is true. **/
    union {
        int32_t i;
        const char *s;
    } default_value;

} config_t;

/**
 * All the configuration properties, indexed by their config_id_t.
 */
static const config_t configs[] = {

    /** Connection **/

    [CONFIG_BROKER_URL] = {
        .category = CONFIG_CATEGORY_CONNECTION, .namespace = CONFIG_NAMESPACE_CONNECTION, .key = "broker_url",
        .type = CONFIG_TYPE_STRING, .writable = true, .readable = true
    },

    [CONFIG_KEEP_ALIVE] = {
        .category = CONFIG_CATEGORY_CONNECTION, .namespace = CONFIG_NAMESPACE_CONNECTION, .key = "keep_alive",
        .type = CONFIG_TYPE_INT, .writable = true, .readable = true, .has_default = true, .default_value.i = 60
    },

    [CONFIG_QOS] = {
        .category = CONFIG_CATEGORY_CONNECTION, .namespace = CONFIG_NAMESPACE_CONNECTION, .key = "qos",
        .type = CONFIG_TYPE_INT, .writable = true, .readable = true, .has_default = true, .default_value.i = 0
    },

    [CONFIG_RECONNECT_INTERVAL] = {
        .category = CONFIG_CATEGORY_CONNECTION, .namespace = CONFIG_NAMESPACE_CONNECTION, .key = "recon_interval",
        .type = CONFIG_TYPE_INT, .writable = true, .readable = true, .has_default = true, .default_value.i = 30
    },

    /** GPS **/

    [CONFIG_GPS_UPDATE_INTERVAL] = {
        .category = CONFIG_CATEGORY_GPS, .namespace = CONFIG_NAMESPACE_GPS, .key = "gps_update",
        .type = CONFIG_TYPE_INT, .writable = true, .readable = true, .has_default = true, .default_value.i = 60
    },

    [CONFIG_GPS_TIMEOUT] = {
        .category = CONFIG_CATEGORY_GPS, .namespace = CONFIG_NAMESPACE_GPS, .key = "gps_timeout",
        .type = CONFIG_TYPE_INT, .writable = true, .readable = true, .has_default = true, .default_value.i = 60
    },

    [CONFIG_GPS_MODE] = {
        .category = CONFIG_CATEGORY_GPS, .namespace = CONFIG_NAMESPACE_GPS, .key = "gps_mode",
        .type = CONFIG_TYPE_STRING, .writable = true, .readable = true, .has_default = true, .default_value.s = "ue-based"
    },

    /** Authentication **/

    [CONFIG_CERTIFICATE] = {
        .category = CONFIG_CATEGORY_AUTHENTICATION, .type = CONFIG_TYPE_STRING, .writable = true, .readable = true
    },

    [CONFIG_CERTIFICATE_AUTHORITY] = {
        .category = CONFIG_CATEGORY_AUTHENTICATION, .type = CONFIG_TYPE_STRING, .writable = true, .readable = true
    },

    [CONFIG_CSR] = {
        .category = CONFIG_CATEGORY_AUTHENTICATION, .type = CONFIG_TYPE_STRING, .writable = false, .readable = true
    },

    [CONFIG_STATUS] = {
        .category = CONFIG_CATEGORY_AUTHENTICATION, .type = CONFIG_TYPE_STRING, .writable = false, .readable = true
    },

    [CONFIG_REVOKE] = {
        .category = CONFIG_CATEGORY_AUTHENTICATION, .type = CONFIG_TYPE_BOOL, .writable = true, .readable = false
    },

    [CONFIG_ENROLL] = {
        .category = CONFIG_CATEGORY_AUTHENTICATION, .type = CONFIG_TYPE_BOOL, .writable = true, .readable = false
    }

};

/**
 *
 * @brief Looks up the configuration item associated with the given ID.
 *
 * @param id the configuration ID to look up.
 * @return a pointer to the configuration item, or NULL if the ID is unknown.
 */
static const config_t *config_lookup(config_id_t id) {

    if (id < 0 || id >= sizeof(configs) / sizeof(configs[0]))
        return nullptr;

    return &configs[id];
}

/**
 *
 * @brief Builds the storage context used to access the given configuration item.
 *
 * @param config the configuration item.
 * @param mode   the access level for the operation.
 * @return the storage context.
 */
static storage_ctx_t config_storage_ctx(const config_t *config, storage_mode_t mode) {
    return (storage_ctx_t){ .namespace = config->namespace, .key = config->key, .mode = mode };
}

/**
 *
 * @brief Checks whether a value is already stored for the given configuration item.
 *
 * @param config the configuration item.
 * @param is_set the output parameter set to true when a value is stored.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t config_is_set(const config_t *config, bool *is_set) {

    const storage_ctx_t ctx = config_storage_ctx(config, STORAGE_MODE_READ_ONLY);
    esp_err_t err = ESP_ERR_INVALID_ARG;

    switch (config->type) {
        case CONFIG_TYPE_INT: {
            int32_t stored;
            err = storage_get_i32(&ctx, &stored);
            break;
        }
        case CONFIG_TYPE_BOOL: {
            uint8_t stored;
            err = storage_get_u8(&ctx, &stored);
            break;
        }
        case CONFIG_TYPE_STRING: {
            size_t size;
            err = storage_get_str_size(&ctx, &size);
            break;
        }
    }

    // A value that was never set is not a failure
    if (err == ESP_ERR_NVS_NOT_FOUND) {
        *is_set = false;
        return ESP_OK;
    }

    if (err == ESP_OK) {
        *is_set = true;
    }

    return err;
}

/**
 *
 * @brief Reads a stored integer into a newly allocated int32_t.
 *
 * @param ctx   the storage context of the value.
 * @param value the output parameter that receives the allocated value, the caller must free it.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t config_read_int(const storage_ctx_t *ctx, void **value) {

    int32_t *result = malloc(sizeof(int32_t));
    if (result == NULL)
        return ESP_ERR_NO_MEM;

    const esp_err_t err = storage_get_i32(ctx, result);
    if (err != ESP_OK) {
        free(result);
        return err;
    }

    *value = result;
    return ESP_OK;
}

/**
 *
 * @brief Reads a stored boolean into a newly allocated bool, booleans are stored as an unsigned 8-bit integer.
 *
 * @param ctx   the storage context of the value.
 * @param value the output parameter that receives the allocated value, the caller must free it.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t config_read_bool(const storage_ctx_t *ctx, void **value) {

    uint8_t stored = 0;

    const esp_err_t err = storage_get_u8(ctx, &stored);
    if (err != ESP_OK)
        return err;

    bool *result = malloc(sizeof(bool));
    if (result == NULL)
        return ESP_ERR_NO_MEM;

    *result = stored != 0;
    *value = result;
    return ESP_OK;
}

/**
 *
 * @brief Reads a stored string into a newly allocated, null-terminated buffer.
 *
 * @param ctx   the storage context of the value.
 * @param value the output parameter that receives the allocated string, the caller must free it.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t config_read_string(const storage_ctx_t *ctx, void **value) {

    size_t size = 0;

    // 1. Get the size so the buffer fits the stored string
    esp_err_t err = storage_get_str_size(ctx, &size);
    if (err != ESP_OK)
        return err;

    // 2. Read the string into a buffer owned by the caller
    char *result = malloc(size);
    if (result == NULL)
        return ESP_ERR_NO_MEM;

    err = storage_get_str(ctx, result, size);
    if (err != ESP_OK) {
        free(result);
        return err;
    }

    *value = result;
    return ESP_OK;
}

/**
 *
 * @brief Gets the name of a security lifecycle state.
 *
 * @param state the security state.
 * @return the name of the state, as written in the lifecycle documentation.
 */
static const char *config_state_name(security_state_t state) {

    switch (state) {
        case SECURITY_STATE_UNPROVISIONED:
            return "UNPROVISIONED";
        case SECURITY_STATE_ENROLLING:
            return "ENROLLING";
        case SECURITY_STATE_ACTIVE:
            return "ACTIVE";
        case SECURITY_STATE_RENEWING:
            return "RENEWING";
        case SECURITY_STATE_EXPIRED:
            return "EXPIRED";
        case SECURITY_STATE_REVOKED:
            return "REVOKED";
    }

    return "UNKNOWN";
}

/**
 *
 * @brief Reads the lifecycle state from the security layer into a newly allocated string.
 *
 * @param value the output parameter that receives the allocated string, the caller must free it.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t config_read_status(void **value) {

    security_context_t context;

    const esp_err_t err = security_get_context(&context);
    if (err != ESP_OK) {
        return err;
    }

    char *result = strdup(config_state_name(context.state));
    if (result == NULL) {
        return ESP_ERR_NO_MEM;
    }

    *value = result;
    return ESP_OK;
}

/**
 *
 * @brief Reads an authentication value, which is owned by the security layer and not stored by the configuration layer.
 *
 * @param id    the configuration ID to read.
 * @param value the output parameter that receives the allocated value, the caller must free it.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t config_read_security(config_id_t id, void **value) {

    switch (id) {
        case CONFIG_CERTIFICATE:
            return security_get_certificate((char **) value);
        case CONFIG_CERTIFICATE_AUTHORITY:
            return security_get_certificate_authority((char **) value);
        case CONFIG_CSR:
            return security_get_csr((char **) value);
        case CONFIG_STATUS:
            return config_read_status(value);
        default:
            return ESP_ERR_NOT_SUPPORTED;
    }
}

/**
 *
 * @brief Writes an authentication value, which is owned by the security layer and not stored by the configuration layer.
 *
 * @param id    the configuration ID to write.
 * @param value the value to write, according to the type of the property.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t config_write_security(config_id_t id, const void *value) {

    switch (id) {
        case CONFIG_CERTIFICATE:
            return security_install_certificate(value);
        case CONFIG_CERTIFICATE_AUTHORITY:
            return security_install_certificate_authority(value);
        case CONFIG_REVOKE:
            return *(const bool *) value ? security_revoke() : ESP_OK;
        case CONFIG_ENROLL:
            return *(const bool *) value ? security_start_enrollment() : ESP_OK;
        default:
            return ESP_ERR_NOT_SUPPORTED;
    }
}

esp_err_t config_init() {

    // 1. Go through the properties that have a default value
    for (size_t i = 0; i < sizeof(configs) / sizeof(configs[0]); i++) {

        const config_t *config = &configs[i];
        if (!config->has_default) {
            continue;
        }

        // 2. Never overwrite a value that is already set, so each default is applied at most once
        bool is_set = false;
        esp_err_t err = config_is_set(config, &is_set);
        if (err != ESP_OK) {
            ESP_LOGE(TAG, "Failed to check configuration id %zu: %s", i, esp_err_to_name(err));
            return err;
        }

        if (is_set) {
            continue;
        }

        // 3. Write the default value
        const void *value = config->type == CONFIG_TYPE_STRING
            ? (const void *) config->default_value.s
            : (const void *) &config->default_value.i;

        err = config_set((config_id_t) i, value);
        if (err != ESP_OK) {
            return err;
        }
    }

    return ESP_OK;
}

esp_err_t config_get(config_id_t id, void **value) {

    // 1. Validate the output parameter
    if (value == NULL) {
        ESP_LOGE(TAG, "Output value is null");
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Find the item and check that it can be read
    const config_t *config = config_lookup(id);
    if (config == nullptr) {
        ESP_LOGE(TAG, "Unknown configuration id %d", id);
        return ESP_ERR_INVALID_ARG;
    }

    // 3. Check that the item is readable
    if (!config->readable) {
        ESP_LOGE(TAG, "Configuration id %d is not readable", id);
        return ESP_ERR_NOT_SUPPORTED;
    }

    // 4. Authentication values come from the security layer, the others from storage according to their type
    esp_err_t err = ESP_ERR_INVALID_ARG;

    if (config->category == CONFIG_CATEGORY_AUTHENTICATION) {
        err = config_read_security(id, value);
    } else {

        const storage_ctx_t ctx = config_storage_ctx(config, STORAGE_MODE_READ_ONLY);

        switch (config->type) {
            case CONFIG_TYPE_INT:
                err = config_read_int(&ctx, value);
                break;
            case CONFIG_TYPE_BOOL:
                err = config_read_bool(&ctx, value);
                break;
            case CONFIG_TYPE_STRING:
                err = config_read_string(&ctx, value);
                break;
        }

    }

    // 5. A value that was never set is not an error worth logging
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to read configuration id %d: %s", id, esp_err_to_name(err));
    }

    return err;
}

esp_err_t config_set(config_id_t id, const void *value) {

    // 1. Validate the input parameter
    if (value == nullptr) {
        ESP_LOGE(TAG, "Value is null");
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Find the item and check that it can be written
    const config_t *config = config_lookup(id);
    if (config == nullptr) {
        ESP_LOGE(TAG, "Unknown configuration id %d", id);
        return ESP_ERR_INVALID_ARG;
    }

    // 3. Check that the item is writable
    if (!config->writable) {
        ESP_LOGE(TAG, "Configuration id %d is not writable", id);
        return ESP_ERR_NOT_SUPPORTED;
    }

    // 4. Authentication values go to the security layer, the others to storage according to their type
    esp_err_t err = ESP_ERR_INVALID_ARG;

    if (config->category == CONFIG_CATEGORY_AUTHENTICATION) {
        err = config_write_security(id, value);
    } else {

        const storage_ctx_t ctx = config_storage_ctx(config, STORAGE_MODE_READ_WRITE);

        switch (config->type) {
            case CONFIG_TYPE_INT:
                err = storage_set_i32(&ctx, *(const int32_t *) value);
                break;
            case CONFIG_TYPE_BOOL:
                err = storage_set_u8(&ctx, *(const bool *) value ? 1 : 0);
                break;
            case CONFIG_TYPE_STRING:
                err = storage_set_str(&ctx, value);
                break;
        }

    }

    // 5. The value itself is not logged, it can be a credential
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to write configuration id %d: %s", id, esp_err_to_name(err));
    } else {
        ESP_LOGI(TAG, "Configuration id %d updated", id);
    }

    return err;
}