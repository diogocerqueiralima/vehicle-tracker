#ifndef UUID_H

#define UUID_H
#define UUID_LEN 16

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
 * @param uuid Pointer to the UUID bytes to convert.
 * @return A pointer to a newly allocated string containing the UUID in string format or NULL if the conversion fails.
 * The caller is responsible for freeing the returned string using free().
 */
char *uuid_to_string(const uint8_t *uuid);

#endif