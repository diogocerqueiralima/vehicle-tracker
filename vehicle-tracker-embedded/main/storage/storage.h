#ifndef STORAGE_H

#define STORAGE_H
#include <stdint.h>
#include "esp_err.h"

/**
 *
 * @brief Initializes the storage system, preparing it for read/write operations.
 *
 * @return ESP_OK on successful initialization, or an appropriate error code on failure.
 */
esp_err_t init_storage();

/**
 *
 * @brief Deinitializes the storage system, releasing any resources it holds.
 *
 * @return ESP_OK on successful deinitialization, or an appropriate error code on failure.
 */
esp_err_t cleanup_storage();

/**
 *
 * @brief Get the size of data stored in non-volatile storage (NVS) for the specified key.
 *
 * @param key The key for which to retrieve the size. Must be a null-terminated string.
 * @param out_len Output parameter that will be set to the size of the stored value in bytes.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t get_data_size(const char* key, size_t* out_len);

/**
 *
 * @brief Save data to non-volatile storage (NVS) under the specified key.
 *
 * @param key The key under which to store the value. Must be a null-terminated string.
 * @param value The value to store.
 * @param len The length of the value in bytes.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t save_data(const char* key, const char* value, size_t len);

/**
 *
 * @brief Load data from non-volatile storage (NVS) for the specified key.
 *
 * @param key The key for which to retrieve the value. Must be a null-terminated string.
 * @param value A buffer to store the retrieved value. Must be large enough to hold the value associated with the key.
 * @param max_len The maximum length of the value to retrieve.
 * @return ESP_OK on success, ESP_ERR_NVS_NOT_FOUND if the key does not exist, or an appropriate error code on failure.
 */
esp_err_t load_data(const char* key, char* value, size_t max_len);

/**
 *
 * @brief Load a 32-bit unsigned integer from non-volatile storage (NVS) for the specified key. The
 * value must have been stored as a fixed-width little-endian integer, the format the mobile
 * application writes over BLE.
 *
 * @param key The key for which to retrieve the value. Must be a null-terminated string.
 * @param out_value Output parameter that will be set to the stored value.
 * @return ESP_OK on success, ESP_ERR_INVALID_SIZE if the stored value is not 4 bytes long, or an
 * appropriate error code on failure.
 */
esp_err_t load_uint32(const char* key, uint32_t* out_value);

#endif
