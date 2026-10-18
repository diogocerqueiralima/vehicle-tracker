#ifndef STORAGE_H

#define STORAGE_H
#include <stddef.h>
#include <stdint.h>

#include "esp_err.h"

/**
 * @brief Access level granted when a namespace is opened for an operation.
 *
 * The mode belongs to the context, not to the namespace, so the same namespace can be accessed
 * read-only in one place and read-write in another.
 */
typedef enum
{
    /** Only reads are allowed. Writes and erases fail with ESP_ERR_NVS_READ_ONLY. */
    STORAGE_MODE_READ_ONLY,

    /** Reads, writes and erases are allowed. Old values are only marked as deleted and can remain in flash. */
    STORAGE_MODE_READ_WRITE,

    /** Same as STORAGE_MODE_READ_WRITE, but every update or erase also purges the previous value from flash. */
    STORAGE_MODE_READ_WRITE_PURGE
} storage_mode_t;

/**
 * @brief Identifies a value in storage: where it lives (namespace), what it is called (key) and how it is accessed (mode).
 */
typedef struct
{
    /** Namespace holding the key. Must be a null-terminated string within the NVS name limit. */
    const char* namespace;

    /** Key of the value. Must be a null-terminated string within the NVS name limit. */
    const char* key;

    /** Access level used for the operation. */
    storage_mode_t mode;
} storage_ctx_t;

/**
 *
 * @brief Initializes the storage system, preparing it for read/write operations.
 *
 * @return ESP_OK on successful initialization, or an appropriate error code on failure.
 */
esp_err_t storage_init();

/**
 *
 * @brief Destroys the storage system, releasing any resources it holds.
 *
 * @return ESP_OK on successful deinitialization, or an appropriate error code on failure.
 */
esp_err_t storage_destroy();

/**
 *
 * @brief Erase the value stored under the context's key.
 *
 * @param ctx Context of the value to erase. Its mode must allow writing.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG on an invalid context, ESP_ERR_NVS_READ_ONLY when the mode is
 * read-only, ESP_ERR_NVS_NOT_FOUND when no value is stored under the key, or an appropriate error code on failure.
 */
esp_err_t storage_erase(const storage_ctx_t* ctx);

/**
 * @name Integer types
 *
 * Getters read the stored value into @p out_value. Setters store @p value under the context's key and commit it.
 * All of them return ESP_OK on success, ESP_ERR_INVALID_ARG on an invalid context or null output,
 * ESP_ERR_NVS_NOT_FOUND (getters) when no value is stored, ESP_ERR_NVS_TYPE_MISMATCH when the stored
 * value has a different type, ESP_ERR_NVS_READ_ONLY (setters) when the mode is read-only, or an appropriate error code on failure.
 * @{
 */
esp_err_t storage_get_u8(const storage_ctx_t* ctx, uint8_t* out_value);
esp_err_t storage_set_u8(const storage_ctx_t* ctx, uint8_t value);
esp_err_t storage_get_i8(const storage_ctx_t* ctx, int8_t* out_value);
esp_err_t storage_set_i8(const storage_ctx_t* ctx, int8_t value);
esp_err_t storage_get_u16(const storage_ctx_t* ctx, uint16_t* out_value);
esp_err_t storage_set_u16(const storage_ctx_t* ctx, uint16_t value);
esp_err_t storage_get_i16(const storage_ctx_t* ctx, int16_t* out_value);
esp_err_t storage_set_i16(const storage_ctx_t* ctx, int16_t value);
esp_err_t storage_get_u32(const storage_ctx_t* ctx, uint32_t* out_value);
esp_err_t storage_set_u32(const storage_ctx_t* ctx, uint32_t value);
esp_err_t storage_get_i32(const storage_ctx_t* ctx, int32_t* out_value);
esp_err_t storage_set_i32(const storage_ctx_t* ctx, int32_t value);
esp_err_t storage_get_u64(const storage_ctx_t* ctx, uint64_t* out_value);
esp_err_t storage_set_u64(const storage_ctx_t* ctx, uint64_t value);
esp_err_t storage_get_i64(const storage_ctx_t* ctx, int64_t* out_value);
esp_err_t storage_set_i64(const storage_ctx_t* ctx, int64_t value);
/** @} */

/**
 * @name Floating-point types
 *
 * Same contract as the integer types.
 * @{
 */
esp_err_t storage_get_float(const storage_ctx_t* ctx, float* out_value);
esp_err_t storage_set_float(const storage_ctx_t* ctx, float value);
esp_err_t storage_get_double(const storage_ctx_t* ctx, double* out_value);
esp_err_t storage_set_double(const storage_ctx_t* ctx, double value);
/** @} */

/**
 *
 * @brief Get the size of the string stored under the context's key.
 *
 * @param ctx Context of the value.
 * @param out_len Output parameter set to the size in bytes, including the null terminator.
 * @return ESP_OK on success, ESP_ERR_NVS_NOT_FOUND when no value is stored, or an appropriate error code on failure.
 */
esp_err_t storage_get_str_size(const storage_ctx_t* ctx, size_t* out_len);

/**
 *
 * @brief Read the zero-terminated string stored under the context's key.
 *
 * @param ctx Context of the value.
 * @param out_value Buffer that receives the string, including the null terminator.
 * @param max_len Size of @p out_value in bytes.
 * @return ESP_OK on success, ESP_ERR_NVS_INVALID_LENGTH when the buffer is too small, ESP_ERR_NVS_NOT_FOUND when no
 * value is stored, or an appropriate error code on failure.
 */
esp_err_t storage_get_str(const storage_ctx_t* ctx, char* out_value, size_t max_len);

/**
 *
 * @brief Store a zero-terminated string under the context's key.
 *
 * @param ctx Context of the value. Its mode must allow writing.
 * @param value Null-terminated string to store.
 * @return ESP_OK on success, ESP_ERR_NVS_READ_ONLY when the mode is read-only, or an appropriate error code on failure.
 */
esp_err_t storage_set_str(const storage_ctx_t* ctx, const char* value);

/**
 *
 * @brief Get the size of the binary blob stored under the context's key.
 *
 * @param ctx Context of the value.
 * @param out_len Output parameter set to the size of the blob in bytes.
 * @return ESP_OK on success, ESP_ERR_NVS_NOT_FOUND when no value is stored, or an appropriate error code on failure.
 */
esp_err_t storage_get_blob_size(const storage_ctx_t* ctx, size_t* out_len);

/**
 *
 * @brief Read the binary blob stored under the context's key.
 *
 * @param ctx Context of the value.
 * @param out_value Buffer that receives the blob.
 * @param max_len Size of @p out_value in bytes.
 * @return ESP_OK on success, ESP_ERR_NVS_INVALID_LENGTH when the buffer is too small, ESP_ERR_NVS_NOT_FOUND when no
 * value is stored, or an appropriate error code on failure.
 */
esp_err_t storage_get_blob(const storage_ctx_t* ctx, void* out_value, size_t max_len);

/**
 *
 * @brief Store a binary blob under the context's key.
 *
 * @param ctx Context of the value. Its mode must allow writing.
 * @param value Blob to store.
 * @param len Length of the blob in bytes. Must be greater than zero.
 * @return ESP_OK on success, ESP_ERR_NVS_READ_ONLY when the mode is read-only, or an appropriate error code on failure.
 */
esp_err_t storage_set_blob(const storage_ctx_t* ctx, const void* value, size_t len);

#endif
