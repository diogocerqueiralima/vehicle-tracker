#ifndef GATT_COMMON_H

#define GATT_COMMON_H

#include <stdbool.h>
#include <stddef.h>
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

/**
 * @brief Context structure for GATT characteristic access callbacks that handle file-like values.
 * Contains the namespace, name, a validation function and the maximum allowed length for the
 * configuration item being accessed, as well as the per-characteristic state of any in-progress
 * read or write sequence. The read/write state is connection-specific, so it is reset on
 * disconnect by gatt_common_on_disconnect().
 */
typedef struct
{
    const char* namespace;
    const char* name;
    bool (*validate)(const char* data, size_t len);
    size_t max_len;
    struct
    {
        uint16_t conn_handle;
        uint8_t* buffer;
        size_t total_len;
        size_t received;
    } write;
    struct
    {
        uint16_t conn_handle;
        bool started;
        size_t cursor;
        uint8_t* buffer;
        size_t total_len;
    } read;
} gatt_file_handler_context_t;

/**
 * @brief GATT characteristic access callback for "file" characteristics, shared by all
 * configuration services. Reassembles chunked writes into NVS storage and serves chunked reads
 * back out of it, using the gatt_file_handler_context_t arg to carry the namespace, validation
 * function, size cap and per-characteristic transfer state. See gatt_file_handler_context_t for
 * the wire format and sequencing rules.
 */
int gatt_common_file_access_cb(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt* ctxt,
                                void* arg);

/**
 * @brief Serves one chunk of a file characteristic's stored value as a read, using the
 * gatt_file_handler_context_t's read state and wire format. Exposed on its own, alongside
 * gatt_common_file_access_cb, so a characteristic with bespoke read-side behavior (e.g.
 * authentication_service's csr, which generates its value on first read instead of expecting it
 * pre-populated) can ensure the value exists and then delegate to the same chunked read logic a
 * plain file characteristic uses.
 */
int gatt_common_file_read_chunk(uint16_t conn_handle, gatt_file_handler_context_t* ctx,
                                 struct ble_gatt_access_ctxt* ctxt);

/**
 * @brief Registers a file characteristic's context so gatt_common_on_disconnect() can tear down
 * whatever read/write sequence it has in progress when its owning connection drops. Every
 * gatt_file_handler_context_t must be registered once, before ble_manager_init() starts accepting
 * connections - otherwise a dropped connection leaks its in-progress write buffer forever and a
 * later connection that NimBLE happens to hand the same conn_handle resumes the read sequence at
 * a stale cursor instead of starting fresh.
 *
 * @param ctx Pointer to the context to register; must remain valid for the life of the program.
 */
void gatt_common_file_context_register(gatt_file_handler_context_t* ctx);

/**
 * @brief Tears down any read/write sequence a just-dropped connection left in progress on any
 * registered file characteristic: frees the cached read buffer or the in-progress write buffer
 * and resets the sequence state. NimBLE reuses conn_handle values across connections, so without
 * this a reconnecting client that gets the same conn_handle back would resume a stale read cursor,
 * and a write buffer for a transfer that never completes would never be freed.
 *
 * @param conn_handle The connection handle that just disconnected.
 */
void gatt_common_on_disconnect(uint16_t conn_handle);

#endif
