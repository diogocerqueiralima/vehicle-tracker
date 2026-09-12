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
 * @brief Context structure for GATT "file" characteristics: values that may be larger than the
 * 512-byte BLE ATT attribute value cap, transferred in chunks framed as
 * [total_len: u32 LE][offset: u32 LE][payload...], one chunk per client-issued GATT operation.
 *
 * The write/read fields are mutable transfer state, not configuration: they track an in-progress
 * write and the current read sequence for this characteristic. There is room for exactly one
 * in-progress transfer per direction per characteristic, which is enough for the single BLE central
 * this device is provisioned by at a time; a read or write from a different conn_handle than the
 * one currently owning a sequence always restarts that sequence rather than being rejected, so a
 * dropped connection can never wedge the characteristic for the next one.
 *
 * The stored value has no partial-read API in NVS (nvs_get_blob always reads the whole blob), so a
 * read sequence loads it into read.buffer once, on its first chunk, and serves every subsequent
 * chunk of that same sequence out of that cached copy instead of reloading the whole value again
 * per chunk; the buffer is freed once the sequence completes or is abandoned for a new one.
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

#endif
