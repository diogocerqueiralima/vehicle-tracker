#ifndef UUID_H

#define UUID_H
#define UUID_LEN 16

/** Size of the buffer that holds the string form of a UUID: 36 characters and the null terminator. */
#define UUID_STRING_LEN 37

#include <stdint.h>

#include "esp_err.h"

/**
 *
 * @brief Generates a new UUIDv4. The implementation must follow the
 * <a href="https://www.rfc-editor.org/info/rfc9562/#name-uuid-version-4">RFC 9562</a> standard for UUIDv4 generation.
 *
 * @param uuid Pointer to the buffer where the generated UUIDv4 will be stored, which must be at least 16 bytes long.
 * @return ESP_OK on success, or an error code on failure.
 */
esp_err_t uuid_generate_v4(uint8_t *uuid);

/**
 *
 * @brief Converts a UUID to a string representation. The output string will be in the standard UUID format.
 *
 * @param uuid Pointer to the UUID bytes to convert. Must be at least 16 bytes long.
 * @param out Buffer that receives the string, including the null terminator. Must be at least UUID_STRING_LEN bytes long.
 * @return ESP_OK on success, or ESP_ERR_INVALID_ARG if an argument is NULL.
 */
esp_err_t uuid_to_string(const uint8_t *uuid, char *out);

#endif