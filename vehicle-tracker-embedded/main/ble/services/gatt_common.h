#ifndef GATT_COMMON_H

#define GATT_COMMON_H

#include <stdbool.h>
#include <stdint.h>
#include "esp_err.h"
#include "host/ble_gatt.h"

/**
 * @brief Application-specific ATT error code, from the 0x80-0x9F range the Bluetooth specification
 * reserves for the higher-layer profile, returned on a read of a characteristic that was never
 * configured. Distinguishes an expected unconfigured setting from BLE_ATT_ERR_UNLIKELY, which stays
 * reserved for actual storage failures.
 * Picked from the upper half of the range because Android's Bluetooth stack reuses 0x80-0x8F for its
 * own local errors, which a client could not tell apart from an error code sent by this device.
 */
#define GATT_ATT_ERR_NOT_CONFIGURED 0x90

/**
 * @brief Context structure for GATT characteristic access callbacks.
 * Contains the namespace, name, a validation function and the documented default value for the
 * configuration item being accessed, allowing the same callback function to handle multiple characteristics.
 */
typedef struct
{
    const char* namespace;
    const char* name;
    bool (*validate)(const char* data, uint16_t len);
    const char* default_value;
    uint16_t default_len;
} gatt_handler_context_t;

/**
 * @brief Generic GATT characteristic access callback shared by all configuration services.
 * Reads/writes the value from/to NVS storage using the namespace in the gatt_handler_context_t arg,
 * validating writes with the context's validate function if provided.
 */
int gatt_common_access_cb(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt* ctxt, void* arg);

/**
 * @brief Writes the documented default value of every characteristic of the service that was never configured.
 * Characteristics that already hold a value, and those declaring no default in their gatt_handler_context_t,
 * are left untouched, so a read on a fresh device returns the documented default instead of an error.
 *
 * @param svc_def The service whose characteristics should be seeded.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t gatt_common_seed_defaults(const struct ble_gatt_svc_def* svc_def);

#endif
