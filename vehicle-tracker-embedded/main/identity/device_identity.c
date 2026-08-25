#include "device_identity.h"

#include <stdio.h>
#include "esp_random.h"
#include "storage/storage.h"

// Generates a random UUIDv4 (RFC 4122) and persists it so it survives reboots.
static esp_err_t generate_and_store_id(uint8_t out_id[DEVICE_IDENTITY_ID_LEN])
{

    // 1. Fill with cryptographically strong random bytes
    esp_fill_random(out_id, DEVICE_IDENTITY_ID_LEN);

    // 2. Set the version (0100) and variant (10xx) bits so the value is a valid UUIDv4
    out_id[6] = (out_id[6] & 0x0F) | 0x40;
    out_id[8] = (out_id[8] & 0x3F) | 0x80;

    // 3. Persist so the same identifier is reused on subsequent boots
    return save_data(DEVICE_IDENTITY_NVS_KEY, (const char*)out_id, DEVICE_IDENTITY_ID_LEN);
}

esp_err_t device_identity_get(uint8_t out_id[DEVICE_IDENTITY_ID_LEN])
{
    if (out_id == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 1. Check whether an identifier is already stored
    size_t stored_len = 0;
    const esp_err_t size_err = get_data_size(DEVICE_IDENTITY_NVS_KEY, &stored_len);

    // 2. Load it if present and correctly sized, otherwise this is first boot: generate one
    if (size_err == ESP_OK && stored_len == DEVICE_IDENTITY_ID_LEN)
    {
        return load_data(DEVICE_IDENTITY_NVS_KEY, (char*)out_id, DEVICE_IDENTITY_ID_LEN);
    }

    return generate_and_store_id(out_id);
}

void device_identity_to_string(const uint8_t id[DEVICE_IDENTITY_ID_LEN], char out_str[DEVICE_IDENTITY_STRING_LEN + 1])
{

    // RFC 4122 canonical form groups the 16 bytes as 4-2-2-2-6 (8-4-4-4-12 hex digits).
    snprintf(
        out_str, DEVICE_IDENTITY_STRING_LEN + 1,
        "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
        id[0], id[1], id[2], id[3], id[4], id[5], id[6], id[7],
        id[8], id[9], id[10], id[11], id[12], id[13], id[14], id[15]
    );

}
