#ifndef GATT_COMMON_H

#define GATT_COMMON_H

#include <stdbool.h>
#include <stdint.h>
#include "host/ble_gatt.h"

/**
 * @brief Context structure for GATT characteristic access callbacks.
 * Contains the namespace, name, and a validation function for the configuration item being accessed,
 * allowing the same callback function to handle multiple characteristics.
 */
typedef struct
{
    const char* namespace;
    const char* name;
    bool (*validate)(const char* data, uint16_t len);
} gatt_handler_context_t;

/**
 * @brief Generic GATT characteristic access callback shared by all configuration services.
 * Reads/writes the value from/to NVS storage using the namespace in the gatt_handler_context_t arg,
 * validating writes with the context's validate function if provided.
 */
int gatt_common_access_cb(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt* ctxt, void* arg);

#endif
