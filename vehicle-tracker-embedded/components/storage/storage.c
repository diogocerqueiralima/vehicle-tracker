#include "storage.h"

#include <string.h>

#include "nvs.h"
#include "nvs_flash.h"

/**
 * @brief Checks that the context is complete and that its names fit the NVS limits.
 *
 * @param ctx Context to validate.
 * @return true if the context is valid, false otherwise.
 */
static bool is_ctx_valid(const storage_ctx_t *ctx) {
    return ctx != nullptr && ctx->namespace != nullptr && ctx->key != nullptr
           && strlen(ctx->namespace) > 0 && strlen(ctx->namespace) < NVS_KEY_NAME_MAX_SIZE
           && strlen(ctx->key) > 0 && strlen(ctx->key) < NVS_KEY_NAME_MAX_SIZE;
}

/**
 * @brief Maps the storage mode to the NVS open mode.
 *
 * @return NVS_READONLY, NVS_READWRITE or NVS_READWRITE_PURGE
 */
static nvs_open_mode_t to_nvs_mode(const storage_mode_t mode) {

    switch (mode) {
        case STORAGE_MODE_READ_WRITE:
            return NVS_READWRITE;
        case STORAGE_MODE_READ_WRITE_PURGE:
            return NVS_READWRITE_PURGE;
        case STORAGE_MODE_READ_ONLY:
        default:
            return NVS_READONLY;
    }

}

/**
 * @brief Validates the context and opens its namespace with the requested mode.
 *
 * @param ctx Context to open.
 * @param writable Whether the operation modifies data, which requires a read-write mode.
 * @param out_handle Receives the NVS handle on success. The caller must close it.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t open_ctx(const storage_ctx_t *ctx, const bool writable, nvs_handle_t *out_handle) {

    // 1. Reject incomplete contexts and names that exceed the NVS limit
    if (!is_ctx_valid(ctx)) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Reject modifications through a read-only context before touching flash
    if (writable && ctx->mode == STORAGE_MODE_READ_ONLY) {
        return ESP_ERR_NVS_READ_ONLY;
    }

    // 3. Open the namespace with the access level of the context
    return nvs_open(ctx->namespace, to_nvs_mode(ctx->mode), out_handle);
}

/**
 * @brief Stores a blob under the context's key, which also backs the floating-point types.
 *
 * @param ctx Context of the value.
 * @param value Pointer to the data to store.
 * @param len Length of the data to store.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t write_blob(const storage_ctx_t *ctx, const void *value, const size_t len) {

    nvs_handle_t handle;

    // 1. Open the namespace for writing
    esp_err_t err = open_ctx(ctx, true, &handle);
    if (err != ESP_OK) {
        return err;
    }

    // 2. Write the value and commit it only if the write succeeded
    err = nvs_set_blob(handle, ctx->key, value, len);
    if (err == ESP_OK) {
        err = nvs_commit(handle);
    }

    // 3. Close the handle
    nvs_close(handle);
    return err;
}

/**
 * @brief Reads a blob of exactly @p len bytes from the context's key.
 *
 * @param ctx Context of the value.
 * @param out_value Buffer that receives the blob.
 * @param len Length of the blob to read.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t read_blob(const storage_ctx_t *ctx, void *out_value, const size_t len) {

    nvs_handle_t handle;
    size_t stored_len = len;

    // 1. Open the namespace for reading
    const esp_err_t err = open_ctx(ctx, false, &handle);
    if (err != ESP_OK) {
        return err;
    }

    // 2. Read the value into the caller's buffer
    const esp_err_t read_err = nvs_get_blob(handle, ctx->key, out_value, &stored_len);

    // 3. Close the handle
    nvs_close(handle);
    return read_err;
}

/**
 * @brief Generates a getter and a setter for an integer type backed by the matching nvs_get_* / nvs_set_* pair.
 *
 * @param suffix The suffix to use for the generated functions.
 * @param type The type of the integer.
 * @param nvs_suffix The suffix to use for the underlying nvs functions.
 */
#define STORAGE_DEFINE_INT(suffix, type, nvs_suffix)                                          \
    esp_err_t storage_get_##suffix(const storage_ctx_t* ctx, type* out_value) {               \
        if (out_value == nullptr) {                                                           \
            return ESP_ERR_INVALID_ARG;                                                       \
        }                                                                                     \
        nvs_handle_t handle;                                                                  \
        esp_err_t err = open_ctx(ctx, false, &handle);                                        \
        if (err != ESP_OK) {                                                                  \
            return err;                                                                       \
        }                                                                                     \
        err = nvs_get_##nvs_suffix(handle, ctx->key, out_value);                              \
        nvs_close(handle);                                                                    \
        return err;                                                                           \
    }                                                                                         \
                                                                                              \
    esp_err_t storage_set_##suffix(const storage_ctx_t* ctx, const type value) {              \
        nvs_handle_t handle;                                                                  \
        esp_err_t err = open_ctx(ctx, true, &handle);                                         \
        if (err != ESP_OK) {                                                                  \
            return err;                                                                       \
        }                                                                                     \
        err = nvs_set_##nvs_suffix(handle, ctx->key, value);                                  \
        if (err == ESP_OK) {                                                                  \
            err = nvs_commit(handle);                                                         \
        }                                                                                     \
        nvs_close(handle);                                                                    \
        return err;                                                                           \
    }

esp_err_t storage_init() {

    esp_err_t err = nvs_flash_init();
    if (err == ESP_ERR_NVS_NO_FREE_PAGES || err == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        // NVS partition was truncated or has a newer format, so it needs to be erased and initialized again
        err = nvs_flash_erase();
        if (err != ESP_OK) {
            return err;
        }

        err = nvs_flash_init();
    }

    return err;
}

esp_err_t storage_destroy() {
    return nvs_flash_deinit();
}

esp_err_t storage_erase(const storage_ctx_t *ctx) {

    nvs_handle_t handle;

    // 1. Open the namespace for writing
    esp_err_t err = open_ctx(ctx, true, &handle);
    if (err != ESP_OK) {
        return err;
    }

    // 2. Remove the key, which reports ESP_ERR_NVS_NOT_FOUND when there is nothing to erase
    err = nvs_erase_key(handle, ctx->key);

    // 3. Commit the removal
    if (err == ESP_OK) {
        err = nvs_commit(handle);
    }

    // 4. Close the handle
    nvs_close(handle);
    return err;
}

STORAGE_DEFINE_INT(u8, uint8_t, u8)
STORAGE_DEFINE_INT(i8, int8_t, i8)
STORAGE_DEFINE_INT(u16, uint16_t, u16)
STORAGE_DEFINE_INT(i16, int16_t, i16)
STORAGE_DEFINE_INT(u32, uint32_t, u32)
STORAGE_DEFINE_INT(i32, int32_t, i32)
STORAGE_DEFINE_INT(u64, uint64_t, u64)
STORAGE_DEFINE_INT(i64, int64_t, i64)

esp_err_t storage_get_float(const storage_ctx_t *ctx, float *out_value) {

    if (out_value == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    return read_blob(ctx, out_value, sizeof(float));
}

esp_err_t storage_set_float(const storage_ctx_t *ctx, const float value) {
    return write_blob(ctx, &value, sizeof(float));
}

esp_err_t storage_get_double(const storage_ctx_t *ctx, double *out_value) {
    
    if (out_value == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    return read_blob(ctx, out_value, sizeof(double));
}

esp_err_t storage_set_double(const storage_ctx_t *ctx, const double value) {
    return write_blob(ctx, &value, sizeof(double));
}

esp_err_t storage_get_str_size(const storage_ctx_t *ctx, size_t *out_len) {

    if (out_len == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    nvs_handle_t handle;

    // 1. Open the namespace for reading
    esp_err_t err = open_ctx(ctx, false, &handle);
    if (err != ESP_OK) {
        return err;
    }

    // 2. Pass a null buffer so NVS only reports the stored size
    err = nvs_get_str(handle, ctx->key, nullptr, out_len);

    // 3. Close the handle
    nvs_close(handle);
    return err;
}

esp_err_t storage_get_str(const storage_ctx_t *ctx, char *out_value, const size_t max_len) {

    if (out_value == nullptr || max_len == 0) {
        return ESP_ERR_INVALID_ARG;
    }

    nvs_handle_t handle;
    size_t len = max_len;

    // 1. Open the namespace for reading
    esp_err_t err = open_ctx(ctx, false, &handle);
    if (err != ESP_OK) {
        return err;
    }

    // 2. Read the string, which fails with ESP_ERR_NVS_INVALID_LENGTH when the buffer is too small
    err = nvs_get_str(handle, ctx->key, out_value, &len);

    // 3. Close the handle
    nvs_close(handle);
    return err;
}

esp_err_t storage_set_str(const storage_ctx_t *ctx, const char *value) {

    if (value == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    nvs_handle_t handle;

    // 1. Open the namespace for writing
    esp_err_t err = open_ctx(ctx, true, &handle);
    if (err != ESP_OK) {
        return err;
    }

    // 2. Write the string and commit it only if the write succeeded
    err = nvs_set_str(handle, ctx->key, value);
    if (err == ESP_OK) {
        err = nvs_commit(handle);
    }

    // 3. Close the handle
    nvs_close(handle);
    return err;
}

esp_err_t storage_get_blob_size(const storage_ctx_t *ctx, size_t *out_len) {

    if (out_len == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    nvs_handle_t handle;

    // 1. Open the namespace for reading
    esp_err_t err = open_ctx(ctx, false, &handle);
    if (err != ESP_OK) {
        return err;
    }

    // 2. Pass a null buffer so NVS only reports the stored size
    err = nvs_get_blob(handle, ctx->key, nullptr, out_len);

    // 3. Close the handle
    nvs_close(handle);
    return err;
}

esp_err_t storage_get_blob(const storage_ctx_t *ctx, void *out_value, const size_t max_len) {

    if (out_value == nullptr || max_len == 0) {
        return ESP_ERR_INVALID_ARG;
    }

    return read_blob(ctx, out_value, max_len);
}

esp_err_t storage_set_blob(const storage_ctx_t *ctx, const void *value, const size_t len) {

    if (value == nullptr || len == 0) {
        return ESP_ERR_INVALID_ARG;
    }

    return write_blob(ctx, value, len);
}
