#include "gatt_common.h"

#include <inttypes.h>
#include <stdlib.h>
#include <string.h>
#include "esp_log.h"
#include "host/ble_att.h"
#include "nvs.h"
#include "storage/storage.h"

static const char *LOG_TAG = "gatt_common";

// Byte length of the [total_len][offset] header framing every read/write chunk.
#define GATT_FILE_CHUNK_HEADER_LEN 8

static void put_u32_le(uint8_t *buf, const uint32_t value) {
    buf[0] = (uint8_t) (value & 0xFF);
    buf[1] = (uint8_t) ((value >> 8) & 0xFF);
    buf[2] = (uint8_t) ((value >> 16) & 0xFF);
    buf[3] = (uint8_t) ((value >> 24) & 0xFF);
}

static uint32_t get_u32_le(const uint8_t *buf) {
    return (uint32_t) buf[0] | ((uint32_t) buf[1] << 8) | ((uint32_t) buf[2] << 16) | ((uint32_t) buf[3] << 24);
}

/**
 *
 * @brief Resets the write state of a GATT file handler context, freeing any allocated buffer and resetting the total length and received length to zero.
 *
 * @param ctx the GATT file handler context whose write state is to be reset
 */
static void reset_write_state(gatt_file_handler_context_t *ctx) {
    if (ctx->write.buffer != nullptr) {
        free(ctx->write.buffer);
        ctx->write.buffer = nullptr;
    }

    ctx->write.total_len = 0;
    ctx->write.received = 0;
}

// Forward declaration: gatt_file_write_chunk() needs to drop a stale cached read sequence once it
// overwrites the same characteristic's stored value, but abandon_read_sequence() is defined below it.
static void abandon_read_sequence(gatt_file_handler_context_t *ctx);

int gatt_common_access_cb(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt *ctxt, void *arg) {
    (void) conn_handle;
    (void) attr_handle;

    const gatt_handler_context_t *ctx = (gatt_handler_context_t *) arg;

    switch (ctxt->op) {
        case BLE_GATT_ACCESS_OP_READ_CHR: {
            ESP_LOGI(LOG_TAG, "Reading %s configuration", ctx->name);

            // 1. Get the size of the stored value to determine how much data to read.
            size_t len = 0;
            esp_err_t err = get_data_size(ctx->namespace, &len);

            // 1.1 A missing namespace/key is the expected state of a setting that was never written,
            // so report it as such instead of as a failure.
            if (err == ESP_ERR_NVS_NOT_FOUND) {
                ESP_LOGW(LOG_TAG, "%s is not configured yet", ctx->name);
                return GATT_ATT_ERR_NOT_CONFIGURED;
            }

            if (err != ESP_OK) {
                ESP_LOGE(LOG_TAG, "Failed to get %s size: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 2. Handle the case where no value is stored yet (len == 0) the same way as a missing key.
            if (len == 0) {
                ESP_LOGW(LOG_TAG, "Stored %s value is empty", ctx->name);
                return GATT_ATT_ERR_NOT_CONFIGURED;
            }

            // 3. Read the stored value.
            char buf[len];
            err = load_data(ctx->namespace, buf, len);
            if (err != ESP_OK) {
                ESP_LOGE(LOG_TAG, "Failed to load %s: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 4. Append the value to the response buffer to be sent back to the client.
            if (os_mbuf_append(ctxt->om, buf, len) != 0) {
                return BLE_ATT_ERR_INSUFFICIENT_RES;
            }

            ESP_LOGI(LOG_TAG, "Successfully read %s", ctx->name);
            break;
        }
        case BLE_GATT_ACCESS_OP_WRITE_CHR: {
            ESP_LOGI(LOG_TAG, "Writing %s configuration", ctx->name);

            // 5. Check if the length of the incoming data is valid
            const uint16_t len = OS_MBUF_PKTLEN(ctxt->om);
            if (len <= 0) {
                return BLE_ATT_ERR_INVALID_ATTR_VALUE_LEN;
            }

            // 6. Copy the incoming data from the mbuf into a local buffer for validation and storage.
            char buf[len];
            os_mbuf_copydata(ctxt->om, 0, len, buf);

            // 7. Validate the value using the provided validation function, if any.
            if (ctx->validate != NULL && !ctx->validate(buf, len)) {
                ESP_LOGE(LOG_TAG, "Invalid value for %s:", ctx->name);
                ESP_LOG_BUFFER_HEX_LEVEL(LOG_TAG, buf, len, ESP_LOG_ERROR);
                return BLE_ATT_ERR_VALUE_NOT_ALLOWED;
            }

            // 8. Save the value to NVS for persistent storage.
            const esp_err_t err = save_data(ctx->namespace, buf, len);
            if (err != ESP_OK) {
                ESP_LOGE(LOG_TAG, "Failed to save %s: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            ESP_LOGI(LOG_TAG, "Successfully saved %s", ctx->name);
            break;
        }
        default:
            break;
    }

    return 0;
}

esp_err_t gatt_common_seed_defaults(const struct ble_gatt_svc_def *svc_def) {
    if (svc_def == nullptr || svc_def->characteristics == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 1. Walk the characteristics of the service up to the {0} terminator entry.
    for (const struct ble_gatt_chr_def *chr = svc_def->characteristics; chr->uuid != nullptr; chr++) {
        // 2. File characteristics carry a gatt_file_handler_context_t instead, which has no
        // default_value concept and a different layout - the access_cb is the only thing that
        // tells the two context types apart, since ble_gatt_chr_def itself carries no type tag.
        if (chr->access_cb != gatt_common_access_cb) {
            continue;
        }

        const gatt_handler_context_t *ctx = chr->arg;

        // 3. Skip characteristics that have no context or no documented default value.
        if (ctx == nullptr || ctx->default_value == nullptr || ctx->default_len == 0) {
            continue;
        }

        // 4. Only a characteristic that was never written gets its default: ESP_ERR_NVS_NOT_FOUND means
        // the namespace/key does not exist yet, any other result means the value is already configured
        // (or unreadable) and must be left untouched.
        size_t len = 0;
        const esp_err_t size_err = get_data_size(ctx->namespace, &len);
        if (size_err != ESP_ERR_NVS_NOT_FOUND) {
            continue;
        }

        // 5. Persist the default so subsequent reads of the characteristic succeed.
        const esp_err_t err = save_data(ctx->namespace, ctx->default_value, ctx->default_len);
        if (err != ESP_OK) {
            ESP_LOGE(LOG_TAG, "Failed to seed default %s: %s", ctx->name, esp_err_to_name(err));
            return err;
        }

        ESP_LOGI(LOG_TAG, "Seeded default %s", ctx->name);
    }

    return ESP_OK;
}

/**
 *
 * @brief Handles a chunked write of a file characteristic, reassembling the chunks into a complete value and persisting it once all chunks are received.
 *
 * @param conn_handle the connection handle of the BLE connection writing the characteristic
 * @param ctx the gatt_file_handler_context_t structure for the characteristic being written
 * @param ctxt the GATT access context containing the operation type and the mbuf with the chunk data
 * @return the ATT error code, 0 on success, or a specific error code on failure
 */
static int gatt_file_write_chunk(
    const uint16_t conn_handle, gatt_file_handler_context_t *ctx, struct ble_gatt_access_ctxt *ctxt
) {
    // 1. The characteristic is declared write-only, so any other operation is a host-level mismatch.
    const uint16_t len = OS_MBUF_PKTLEN(ctxt->om);
    if (len < GATT_FILE_CHUNK_HEADER_LEN) {
        return BLE_ATT_ERR_INVALID_ATTR_VALUE_LEN;
    }

    // 2. Extract the total length and offset from the chunk header, and calculate the payload length.
    uint8_t header[GATT_FILE_CHUNK_HEADER_LEN];
    os_mbuf_copydata(ctxt->om, 0, GATT_FILE_CHUNK_HEADER_LEN, header);
    const uint32_t total_len = get_u32_le(header);
    const uint32_t offset = get_u32_le(header + 4);
    const size_t payload_len = len - GATT_FILE_CHUNK_HEADER_LEN;

    if (offset == 0) {
        
        // 3. If this is the first chunk, initialize the write state.
        reset_write_state(ctx);

        // 4. Validate the total length against the maximum allowed length for this characteristic.
        if (total_len == 0 || total_len > ctx->max_len) {
            ESP_LOGE(LOG_TAG, "Rejected %s write: total_len %" PRIu32 " is invalid or exceeds the %zu byte cap",
                     ctx->name, total_len, ctx->max_len);
            return BLE_ATT_ERR_VALUE_NOT_ALLOWED;
        }

        // 5. Allocate a buffer to hold the complete value being written.
        ctx->write.buffer = malloc(total_len);
        if (ctx->write.buffer == nullptr) {
            return BLE_ATT_ERR_INSUFFICIENT_RES;
        }

        // 6. Initialize the write state with the total length, received length, and connection handle.
        ctx->write.total_len = total_len;
        ctx->write.received = 0;
        ctx->write.conn_handle = conn_handle;
    }
    // 7. If this is not the first chunk, validate that it is part of the same write sequence and that the offset matches the expected received length.
    else if (ctx->write.buffer == nullptr || conn_handle != ctx->write.conn_handle ||
             total_len != ctx->write.total_len || offset != ctx->write.received) {
        ESP_LOGE(LOG_TAG, "Rejected out-of-sequence %s write chunk at offset %" PRIu32, ctx->name, offset);
        reset_write_state(ctx);
        return BLE_ATT_ERR_VALUE_NOT_ALLOWED;
    }

    // 8. Validate that the chunk does not overrun the declared total length.
    if ((size_t) offset + payload_len > ctx->write.total_len) {
        ESP_LOGE(LOG_TAG, "Rejected %s write chunk: payload overruns the declared total_len", ctx->name);
        reset_write_state(ctx);
        return BLE_ATT_ERR_VALUE_NOT_ALLOWED;
    }

    // 9. Copy the chunk payload into the allocated buffer at the specified offset and update the received length.
    os_mbuf_copydata(ctxt->om, GATT_FILE_CHUNK_HEADER_LEN, (int) payload_len, ctx->write.buffer + offset);
    ctx->write.received = offset + payload_len;

    // 10. If this is not the last chunk, return success to indicate that the chunk was accepted and more chunks are expected.
    if (ctx->write.received < ctx->write.total_len) {
        return 0;
    }

    // 11. If this is the last chunk, validate the complete value using the provided validation function, if any.
    if (ctx->validate != nullptr && !ctx->validate((const char *) ctx->write.buffer, ctx->write.total_len)) {
        ESP_LOGE(LOG_TAG, "Invalid value for %s", ctx->name);
        reset_write_state(ctx);
        return BLE_ATT_ERR_VALUE_NOT_ALLOWED;
    }

    // 12. Persist the complete value to NVS for storage, and reset the write state.
    const esp_err_t err = save_data(ctx->namespace, (const char *) ctx->write.buffer, ctx->write.total_len);
    reset_write_state(ctx);

    // 13. If the save operation failed, log the error and return an ATT error code indicating an unlikely failure.
    if (err != ESP_OK) {
        ESP_LOGE(LOG_TAG, "Failed to save %s: %s", ctx->name, esp_err_to_name(err));
        return BLE_ATT_ERR_UNLIKELY;
    }

    // 14. Drop any read sequence still caching the value this write just replaced, so a read
    // chunk request that follows on another connection - or this same one, interleaved between
    // chunks of an in-progress read - serves the new value instead of the stale cached one.
    abandon_read_sequence(ctx);

    ESP_LOGI(LOG_TAG, "Successfully saved %s", ctx->name);
    return 0;
}

/**
 *
 * @brief Abandons the current read sequence for a file characteristic, freeing any cached buffer and resetting the read state.
 *
 * @param ctx Pointer to the gatt_file_handler_context_t structure for the characteristic being read.
 */
static void abandon_read_sequence(gatt_file_handler_context_t *ctx) {

    // 1. Free the cached copy of the stored value, if any, so the next read sequence starts fresh.
    if (ctx->read.buffer != nullptr) {
        free(ctx->read.buffer);
        ctx->read.buffer = nullptr;
    }

    // 2. Reset the read sequence state so the next read (from this connection or another) starts from offset 0.
    ctx->read.total_len = 0;
    ctx->read.started = false;
}

void gatt_common_file_context_invalidate(gatt_file_handler_context_t *ctx) {
    abandon_read_sequence(ctx);
    reset_write_state(ctx);
}

static gatt_file_handler_context_t **file_contexts = nullptr;
static size_t file_context_count = 0;

void gatt_common_file_context_register(gatt_file_handler_context_t *ctx) {

    gatt_file_handler_context_t **tmp = realloc(file_contexts, (file_context_count + 1) * sizeof(*file_contexts));
    if (tmp == nullptr) {
        ESP_LOGE(LOG_TAG, "Failed to register %s for disconnect cleanup", ctx->name);
        return;
    }

    file_contexts = tmp;
    file_contexts[file_context_count++] = ctx;
}

void gatt_common_on_disconnect(const uint16_t conn_handle) {
    for (size_t i = 0; i < file_context_count; i++) {
        gatt_file_handler_context_t *ctx = file_contexts[i];

        if (ctx->read.started && ctx->read.conn_handle == conn_handle) {
            ESP_LOGI(LOG_TAG, "Connection %d dropped mid-read of %s, abandoning its read sequence",
                     conn_handle, ctx->name);
            abandon_read_sequence(ctx);
        }

        if (ctx->write.buffer != nullptr && ctx->write.conn_handle == conn_handle) {
            ESP_LOGI(LOG_TAG, "Connection %d dropped mid-write of %s, discarding its write buffer",
                     conn_handle, ctx->name);
            reset_write_state(ctx);
        }
    }
}

/**
 *
 * @brief Handles a chunked read of a file characteristic, serving the requested chunk from a cached copy of the stored value.
 *
 * @param conn_handle the connection handle of the BLE connection requesting the read
 * @param ctx the gatt_file_handler_context_t structure for the characteristic being read
 * @param ctxt the GATT access context containing the operation type and the mbuf to append the chunk to
 * @return the ATT error code, 0 on success, or a specific error code on failure
 */
int gatt_common_file_read_chunk(const uint16_t conn_handle, gatt_file_handler_context_t *ctx,
                                struct ble_gatt_access_ctxt *ctxt) {
    // 1. If this is the first read from this connection, or a different connection than the one
    // currently mid-sequence, drop any cached buffer from that other sequence and start fresh.
    const bool new_sequence = !ctx->read.started || ctx->read.conn_handle != conn_handle;
    if (new_sequence) {
        abandon_read_sequence(ctx);
        ctx->read.started = true;
        ctx->read.conn_handle = conn_handle;
        ctx->read.cursor = 0;
    }

    // 2. Load the stored value once per sequence, on its first chunk, and reuse it for every
    // later chunk instead of reloading the whole value from NVS on every single read.
    if (ctx->read.buffer == nullptr) {

        size_t total_len = 0;
        const esp_err_t size_err = get_data_size(ctx->namespace, &total_len);

        // 2.1 A missing namespace/key or an empty entry is the expected state of a setting that
        // was never written, so report it as such instead of as a failure.
        if (size_err == ESP_ERR_NVS_NOT_FOUND || (size_err == ESP_OK && total_len == 0)) {
            ESP_LOGW(LOG_TAG, "%s is not configured yet", ctx->name);
            abandon_read_sequence(ctx);
            return GATT_ATT_ERR_NOT_CONFIGURED;
        }

        if (size_err != ESP_OK) {
            ESP_LOGE(LOG_TAG, "Failed to get %s size: %s", ctx->name, esp_err_to_name(size_err));
            abandon_read_sequence(ctx);
            return BLE_ATT_ERR_UNLIKELY;
        }

        // 2.2 Honor the cap on the read side too, instead of allocating whatever size NVS reports.
        if (total_len > ctx->max_len) {
            ESP_LOGE(LOG_TAG, "Stored %s value of %zu bytes exceeds the %zu byte cap", ctx->name, total_len,
                     ctx->max_len);
            abandon_read_sequence(ctx);
            return BLE_ATT_ERR_UNLIKELY;
        }

        uint8_t *buffer = malloc(total_len);
        if (buffer == nullptr) {
            abandon_read_sequence(ctx);
            return BLE_ATT_ERR_INSUFFICIENT_RES;
        }

        const esp_err_t load_err = load_data(ctx->namespace, (char *) buffer, total_len);
        if (load_err != ESP_OK) {
            ESP_LOGE(LOG_TAG, "Failed to load %s: %s", ctx->name, esp_err_to_name(load_err));
            free(buffer);
            abandon_read_sequence(ctx);
            return BLE_ATT_ERR_UNLIKELY;
        }

        ctx->read.buffer = buffer;
        ctx->read.total_len = total_len;
    }

    // 3. Serve the next chunk of the cached value, up to the maximum payload size allowed by the current connection's MTU.
    const uint16_t mtu = ble_att_mtu(conn_handle);

    // 4. The MTU must be large enough to hold the 2-byte ATT header and the 8-byte chunk header, otherwise no payload can be sent.
    if (mtu <= 2 + GATT_FILE_CHUNK_HEADER_LEN) {
        ESP_LOGE(LOG_TAG, "MTU %d is too small to send any %s chunk", mtu, ctx->name);
        abandon_read_sequence(ctx);
        return BLE_ATT_ERR_INSUFFICIENT_RES;
    }

    const size_t max_chunk_payload = (size_t) mtu - 2 - GATT_FILE_CHUNK_HEADER_LEN;
    const size_t total_len = ctx->read.total_len;
    const size_t remaining = total_len - ctx->read.cursor;
    const size_t chunk_len = remaining < max_chunk_payload ? remaining : max_chunk_payload;

    uint8_t header[GATT_FILE_CHUNK_HEADER_LEN];
    put_u32_le(header, total_len);
    put_u32_le(header + 4, ctx->read.cursor);

    // 5. Append the chunk header
    if (os_mbuf_append(ctxt->om, header, GATT_FILE_CHUNK_HEADER_LEN) != 0) {
        abandon_read_sequence(ctx);
        return BLE_ATT_ERR_INSUFFICIENT_RES;
    }

    // 6. Append the chunk payload
    if (os_mbuf_append(ctxt->om, ctx->read.buffer + ctx->read.cursor, chunk_len) != 0) {
        abandon_read_sequence(ctx);
        return BLE_ATT_ERR_INSUFFICIENT_RES;
    }

    ctx->read.cursor += chunk_len;
    const bool done = ctx->read.cursor >= total_len;

    ESP_LOGI(LOG_TAG, "Successfully read %s chunk (%zu/%zu bytes)", ctx->name, ctx->read.cursor, total_len);

    // 7. Once the sequence is fully served, drop the cached copy and reset the cursor so the next
    // read (from this connection or another) starts a fresh pass from the beginning.
    if (done) {
        abandon_read_sequence(ctx);
    }

    return 0;
}

int gatt_common_file_access_cb(
    const uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt *ctxt,
    void *arg) {
    (void) attr_handle;

    gatt_file_handler_context_t *ctx = arg;

    switch (ctxt->op) {
        case BLE_GATT_ACCESS_OP_READ_CHR:
            return gatt_common_file_read_chunk(conn_handle, ctx, ctxt);
        case BLE_GATT_ACCESS_OP_WRITE_CHR:
            return gatt_file_write_chunk(conn_handle, ctx, ctxt);
        default:
            return 0;
    }
}
