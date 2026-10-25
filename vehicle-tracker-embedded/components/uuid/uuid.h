#ifndef UUID_H

#define UUID_H

/** Size of a UUID in its binary form, in bytes. */
#define UUID_BYTES_SIZE 16

/** Length of a UUID in its canonical textual form (8-4-4-4-12), without the null terminator. */
#define UUID_STR_LEN 36

/** Size of a buffer able to hold a UUID in its canonical textual form, including the null terminator. */
#define UUID_STR_SIZE (UUID_STR_LEN + 1)

#include <stdint.h>
#include "esp_err.h"

/**
 * @brief A UUID in its binary form, in network byte order as defined by RFC 4122.
 */
typedef struct
{
    /** Raw bytes of the UUID. */
    uint8_t bytes[UUID_BYTES_SIZE];
} uuid_t;

/**
 *
 * @brief Generate a random (version 4) UUID.
 *
 * The version and variant bits are set as defined by RFC 4122, the remaining bits are random.
 *
 * @param out_uuid Output parameter set to the generated UUID.
 * @return ESP_OK on success, or ESP_ERR_INVALID_ARG when @p out_uuid is null.
 */
esp_err_t uuid_generate_v4(uuid_t* out_uuid);

/**
 *
 * @brief Convert a UUID to its canonical lowercase textual form, for example "123e4567-e89b-42d3-a456-426614174000".
 *
 * @param uuid UUID to convert.
 * @param out_value Buffer that receives the string, including the null terminator.
 * @param max_len Size of @p out_value in bytes. Must be at least UUID_STR_SIZE.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when @p uuid or @p out_value is null, or ESP_ERR_INVALID_SIZE when
 * the buffer is too small.
 */
esp_err_t uuid_to_str(const uuid_t* uuid, char* out_value, size_t max_len);

#endif
