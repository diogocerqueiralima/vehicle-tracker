#include "uuid.h"

#include "esp_random.h"

esp_err_t uuid_generate_v4(uuid_t *out_uuid) {

    // 1. Reject a missing output
    if (out_uuid == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Fill all bytes with random data
    esp_fill_random(out_uuid->bytes, UUID_BYTES_SIZE);

    // 3. Set the version 4
    out_uuid->bytes[6] = (out_uuid->bytes[6] & 0x0F) | 0x40;

    // 4. Set the variant (binary 10xx) in the high bits of byte 8
    out_uuid->bytes[8] = (out_uuid->bytes[8] & 0x3F) | 0x80;

    return ESP_OK;
}

esp_err_t uuid_to_str(const uuid_t *uuid, char *out_value, const size_t max_len) {

    // 1. Reject missing arguments and buffers that cannot hold the text and the terminator
    if (uuid == nullptr || out_value == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    if (max_len < UUID_STR_SIZE) {
        return ESP_ERR_INVALID_SIZE;
    }

    // 2. Format the bytes into the canonical 8-4-4-4-12 representation, which also adds the null terminator
    const uint8_t *b = uuid->bytes;
    snprintf(
        out_value, max_len, "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
        b[0], b[1], b[2], b[3],
        b[4], b[5],
        b[6], b[7],
        b[8], b[9],
        b[10], b[11], b[12], b[13], b[14], b[15]
    );

    return ESP_OK;
}