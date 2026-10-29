#include "config.h"

#define CONFIG_NAMESPACE_CONNECTION "connection"
#define CONFIG_NAMESPACE_GPS "gps"
#define CONFIG_NAMESPACE_AUTH "auth"

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

    char *namespace;

    config_type_t type;

    bool writable;
    bool readable;

    /** Only used by CONFIG_TYPE_INT and CONFIG_TYPE_STRING, a zeroed value means there is no default. **/
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
        .namespace = CONFIG_NAMESPACE_CONNECTION, .type = CONFIG_TYPE_STRING,
        .writable = true, .readable = true
    },

    [CONFIG_KEEP_ALIVE] = {
        .namespace = CONFIG_NAMESPACE_CONNECTION, .type = CONFIG_TYPE_INT,
        .writable = true, .readable = true, .default_value.i = 60
    },

    [CONFIG_QOS] = {
        .namespace = CONFIG_NAMESPACE_CONNECTION, .type = CONFIG_TYPE_INT,
        .writable = true, .readable = true, .default_value.i = 0
    },

    [CONFIG_RECONNECT_INTERVAL] = {
        .namespace = CONFIG_NAMESPACE_CONNECTION, .type = CONFIG_TYPE_INT,
        .writable = true, .readable = true, .default_value.i = 30
    },

    /** GPS **/

    [CONFIG_GPS_UPDATE_INTERVAL] = {
        .namespace = CONFIG_NAMESPACE_GPS, .type = CONFIG_TYPE_INT,
        .writable = true, .readable = true, .default_value.i = 60
    },

    [CONFIG_GPS_TIMEOUT] = {
        .namespace = CONFIG_NAMESPACE_GPS, .type = CONFIG_TYPE_INT,
        .writable = true, .readable = true, .default_value.i = 60
    },

    [CONFIG_GPS_MODE] = {
        .namespace = CONFIG_NAMESPACE_GPS, .type = CONFIG_TYPE_STRING,
        .writable = true, .readable = true, .default_value.s = "ue-based"
    },

    /** Authentication **/

    [CONFIG_CERTIFICATE] = {
        .namespace = CONFIG_NAMESPACE_AUTH, .type = CONFIG_TYPE_STRING,
        .writable = true, .readable = true
    },

    [CONFIG_CERTIFICATE_AUTHORITY] = {
        .namespace = CONFIG_NAMESPACE_AUTH, .type = CONFIG_TYPE_STRING,
        .writable = true, .readable = true
    },

    [CONFIG_CSR] = {
        .namespace = CONFIG_NAMESPACE_AUTH, .type = CONFIG_TYPE_STRING,
        .writable = false, .readable = true
    },

    [CONFIG_STATUS] = {
        .namespace = CONFIG_NAMESPACE_AUTH, .type = CONFIG_TYPE_STRING,
        .writable = false, .readable = true
    },

    [CONFIG_REVOKE] = {
        .namespace = CONFIG_NAMESPACE_AUTH, .type = CONFIG_TYPE_BOOL,
        .writable = true, .readable = false
    }

};

/**
 * Looks up the configuration item associated with the given ID.
 *
 * @param id the configuration ID to look up.
 * @return a pointer to the configuration item, or NULL if the ID is unknown.
 */
static const config_t *config_lookup(config_id_t id) {

    if (id < 0 || id >= sizeof(configs) / sizeof(configs[0]))
        return NULL;

    return &configs[id];
}

esp_err_t config_init() {

    return ESP_OK;
}