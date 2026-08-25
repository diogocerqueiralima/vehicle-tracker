#ifndef DEVICE_IDENTITY_H

#define DEVICE_IDENTITY_H

#include <stdint.h>
#include "esp_err.h"

#define DEVICE_IDENTITY_NVS_KEY "device_id"
#define DEVICE_IDENTITY_ID_LEN 16
#define DEVICE_IDENTITY_STRING_LEN 36

/**
 * @brief Loads the device's unique identifier from NVS, generating and persisting a random
 * UUIDv4 on first boot if none is stored yet. Storage must already be initialized.
 *
 * @param out_id Buffer of at least DEVICE_IDENTITY_ID_LEN bytes to receive the raw identifier.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t device_identity_get(uint8_t out_id[DEVICE_IDENTITY_ID_LEN]);

/**
 * @brief Formats a raw device identifier as a canonical, lowercase UUID string
 * (8-4-4-4-12 hex digits separated by hyphens), for QR encoding and display.
 * 
 * @param id Raw identifier, DEVICE_IDENTITY_ID_LEN bytes.
 * @param out_str Buffer of at least DEVICE_IDENTITY_STRING_LEN + 1 bytes to receive the
 * null-terminated UUID string.
 */
void device_identity_to_string(const uint8_t id[DEVICE_IDENTITY_ID_LEN], char out_str[DEVICE_IDENTITY_STRING_LEN + 1]);

#endif
