#include "uuid.h"
#include "esp_random.h"

#include <stdlib.h>

esp_err_t uuid_generate_v4(uint8_t *uuid) {

    // 1. Validate the input buffer
    if (uuid == NULL) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Fill the UUID with random bytes
    esp_fill_random(uuid, UUID_LEN);

    // 3. Set the version to 4
    uuid[6] = (uuid[6] & 0x0F) | 0x40;

    // 4. Set the variant to 10
    uuid[8] = (uuid[8] & 0x3F) | 0x80;

    return ESP_OK;
}

char *uuid_to_string(const uint8_t *uuid) {

    // 1. Validate the input UUID
    if (uuid == NULL) {
        return nullptr;
    }

    // 2. Allocate memory for the string representation of the UUID
    char *uuid_str = malloc(37); // 36 characters + null terminator
    if (uuid_str == NULL) {
        return nullptr;
    }

    // 3. Format the UUID into the string representation
    snprintf(
        uuid_str, 37, "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
        uuid[0], uuid[1], uuid[2], uuid[3],
        uuid[4], uuid[5],
        uuid[6], uuid[7],
        uuid[8], uuid[9],
        uuid[10], uuid[11], uuid[12], uuid[13], uuid[14], uuid[15]
    );

    return uuid_str;
}