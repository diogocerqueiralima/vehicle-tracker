#include "authentication_service.h"

#include <string.h>
#include "gatt_common.h"
#include "host/ble_gatt.h"

// Validates that the CSR is a PEM-encoded certificate signing request.
static bool validate_csr(const char* data, const uint16_t len)
{
    static const char* PEM_HEADER = "-----BEGIN CERTIFICATE REQUEST-----";
    const size_t header_len = strlen(PEM_HEADER);
    return len >= header_len && strncmp(data, PEM_HEADER, header_len) == 0;
}

// Validates that the certificate is a PEM-encoded X.509 certificate.
static bool validate_certificate(const char* data, const uint16_t len)
{
    static const char* PEM_HEADER = "-----BEGIN CERTIFICATE-----";
    const size_t header_len = strlen(PEM_HEADER);
    return len >= header_len && strncmp(data, PEM_HEADER, header_len) == 0;
}

// Validates that the CA certificate is a PEM-encoded X.509 certificate.
static bool validate_ca(const char* data, const uint16_t len)
{
    static const char* PEM_HEADER = "-----BEGIN CERTIFICATE-----";
    const size_t header_len = strlen(PEM_HEADER);
    return len >= header_len && strncmp(data, PEM_HEADER, header_len) == 0;
}

// Validates that the revoke flag is a single byte boolean (0 or 1).
static bool validate_revoke(const char* data, const uint16_t len)
{
    if (len != sizeof(uint8_t))
    {
        return false;
    }

    return (uint8_t)*data <= 1;
}

// Validates that the expiration is a non-empty numeric string representing a duration in seconds.
static bool validate_expiration(const char* data, const uint16_t len)
{
    if (len == 0 || len > 20)
    {
        return false;
    }

    for (uint16_t i = 0; i < len; i++)
    {
        if (data[i] < '0' || data[i] > '9')
        {
            return false;
        }
    }

    return true;
}

static const ble_uuid128_t authentication_service_uuid =
    BLE_UUID128_INIT(0x2e, 0x27, 0x71, 0x2d, 0xfe, 0x1e, 0x49, 0xe7, 0xb5, 0xf1, 0x50, 0xcf, 0x73, 0xf5, 0x7d, 0x34);

static const ble_uuid128_t authentication_csr_uuid =
    BLE_UUID128_INIT(0xa1, 0xb2, 0xc3, 0xd4, 0xe5, 0xf6, 0x08, 0x90, 0x2b, 0xcd, 0xef, 0x12, 0x34, 0x56, 0x78, 0x90);

static const ble_uuid128_t authentication_certificate_uuid =
    BLE_UUID128_INIT(0x61, 0x55, 0x51, 0xac, 0x42, 0xa4, 0x08, 0xe8, 0x36, 0xd8, 0x7b, 0x10, 0x8d, 0x26, 0x5c, 0xdf);

static const ble_uuid128_t authentication_ca_uuid =
    BLE_UUID128_INIT(0xce, 0x7d, 0x5f, 0xc9, 0xbf, 0x7d, 0x42, 0xd5, 0x92, 0x70, 0xc9, 0x53, 0x85, 0x76, 0x89, 0x39);

static const ble_uuid128_t authentication_revoke_uuid =
    BLE_UUID128_INIT(0xaa, 0x41, 0x24, 0xb7, 0x9b, 0xe2, 0x43, 0x4d, 0x9a, 0x9c, 0xf6, 0x87, 0xbc, 0x0c, 0x5b, 0x6e);

static const ble_uuid128_t authentication_expiration_uuid =
    BLE_UUID128_INIT(0x0e, 0x2e, 0x7e, 0x19, 0x67, 0x82, 0x4f, 0x6d, 0x90, 0xd9, 0xb1, 0x73, 0x03, 0xca, 0x85, 0x7c);

static const ble_uuid128_t authentication_status_uuid =
    BLE_UUID128_INIT(0x62, 0xbc, 0x2a, 0xa2, 0x4c, 0x8a, 0x4f, 0x26, 0x8d, 0x56, 0x65, 0xb4, 0x4c, 0xf9, 0xb2, 0xae);

static const struct ble_gatt_chr_def characteristics[] = {
    {
        .uuid = &authentication_csr_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN |
                 BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_WRITE_AUTHEN,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = CSR_NAMESPACE,
            .name = "CSR",
            .validate = validate_csr,
        }
    },
    {
        .uuid = &authentication_certificate_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN |
                 BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_WRITE_AUTHEN,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = CERTIFICATE_NAMESPACE,
            .name = "Certificate",
            .validate = validate_certificate,
        }
    },
    {
        .uuid = &authentication_ca_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN |
                 BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_WRITE_AUTHEN,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = CA_NAMESPACE,
            .name = "CA certificate",
            .validate = validate_ca,
        }
    },
    {
        .uuid = &authentication_revoke_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN |
                 BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_WRITE_AUTHEN,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = REVOKE_NAMESPACE,
            .name = "Revoke",
            .validate = validate_revoke,
        }
    },
    {
        .uuid = &authentication_expiration_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN |
                 BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_WRITE_AUTHEN,
				.val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = EXPIRATION_NAMESPACE,
            .name = "Expiration",
            .validate = validate_expiration,
        }
    },
    {
        .uuid = &authentication_status_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = STATUS_NAMESPACE,
            .name = "Status",
            .validate = nullptr,
        }
    },
    {0},
};

const struct ble_gatt_svc_def authentication_service_def = {
    .type = BLE_GATT_SVC_TYPE_PRIMARY,
    .uuid = &authentication_service_uuid.u,
    .characteristics = characteristics,
};
