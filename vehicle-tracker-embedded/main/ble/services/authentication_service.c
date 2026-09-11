#include "authentication_service.h"

#include <stdlib.h>
#include <string.h>
#include "esp_log.h"
#include "gatt_common.h"
#include "host/ble_gatt.h"
#include "identity/device_credentials.h"
#include "nvs.h"
#include "storage/storage.h"

static const char* LOG_TAG = "authentication_service";

/**
 *
 * @brief GATT characteristic access callback for the CSR characteristic.
 * This function generates a new key pair and a CSR for it on the first read, persists the CSR, and returns it.
 * Subsequent reads return the stored CSR, so the key pair the user is enrolling is not replaced while the
 * certificate for it is being issued.
 *
 * @param conn_handle the connection handle of the BLE connection
 * @param attr_handle the attribute handle of the characteristic being accessed
 * @param ctxt the GATT access context containing the operation type and response buffer
 * @param arg the argument passed to the callback, not used in this function
 * @return the ATT error code, 0 on success, or a specific error code on failure
 */
static int csr_access_cb(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt* ctxt, void* arg)
{
    (void)conn_handle;
    (void)attr_handle;
    (void)arg;

    // 1. The characteristic is declared read-only, so any other operation is a host-level mismatch.
    if (ctxt->op != BLE_GATT_ACCESS_OP_READ_CHR)
    {
        return BLE_ATT_ERR_UNLIKELY;
    }

    ESP_LOGI(LOG_TAG, "Reading CSR");

    // 2. Ask how much is stored, which also tells whether a CSR was ever generated.
    size_t len = 0;
    esp_err_t err = get_data_size(CSR_NAMESPACE, &len);

    // 2.1 A missing namespace/key means this is the first read; anything else is a storage failure.
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND)
    {
        ESP_LOGE(LOG_TAG, "Failed to get CSR size: %s", esp_err_to_name(err));
        return BLE_ATT_ERR_UNLIKELY;
    }

    // 3. Return the CSR of a previous read, treating an empty entry as if nothing was stored.
    if (err == ESP_OK && len > 0)
    {
        char buf[len];
        err = load_data(CSR_NAMESPACE, buf, len);

        if (err != ESP_OK)
        {
            ESP_LOGE(LOG_TAG, "Failed to load CSR: %s", esp_err_to_name(err));
            return BLE_ATT_ERR_UNLIKELY;
        }

        if (os_mbuf_append(ctxt->om, buf, len) != 0)
        {
            return BLE_ATT_ERR_INSUFFICIENT_RES;
        }

        ESP_LOGI(LOG_TAG, "Successfully read CSR");
        return 0;
    }

    // 4. First read: generate the device's key pair, replacing any previous one, and the request bound to it.
    char* pem = device_credentials_generate_csr(&err);
    if (pem == nullptr)
    {
        ESP_LOGE(LOG_TAG, "Failed to generate CSR: %s", esp_err_to_name(err));
        return BLE_ATT_ERR_UNLIKELY;
    }

    // 5. Persist it before handing it out, so later reads return the request the backend is signing.
    const size_t pem_len = strlen(pem);
    err = save_data(CSR_NAMESPACE, pem, pem_len);

    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to save CSR: %s", esp_err_to_name(err));
        free(pem);
        return BLE_ATT_ERR_UNLIKELY;
    }

    // 6. Append the generated request to the response buffer and release the buffer it was built in.
    const int rc = os_mbuf_append(ctxt->om, pem, pem_len);
    free(pem);

    if (rc != 0)
    {
        return BLE_ATT_ERR_INSUFFICIENT_RES;
    }

    ESP_LOGI(LOG_TAG, "Successfully generated CSR");
    return 0;
}

/**
 *
 * @brief GATT characteristic access callback for the revoke characteristic.
 * A write of 1 revokes the device's credentials, deleting the stored CSR, the certificate issued for it and the
 * private key they were bound to, if they exist. A write of 0 asks for nothing and leaves the credentials in
 * place. Once revoked, the device can no longer authenticate, and the next read of the CSR characteristic
 * starts a fresh enrollment with a new key pair.
 *
 * @param conn_handle the connection handle of the BLE connection
 * @param attr_handle the attribute handle of the characteristic being accessed
 * @param ctxt the GATT access context containing the operation type and the written value
 * @param arg the argument passed to the callback, not used in this function
 * @return the ATT error code, 0 on success, or a specific error code on failure
 */
static int revoke_access_cb(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt* ctxt, void* arg)
{
    (void)conn_handle;
    (void)attr_handle;
    (void)arg;

    // 1. The characteristic is declared write-only, so any other operation is a host-level mismatch.
    if (ctxt->op != BLE_GATT_ACCESS_OP_WRITE_CHR)
    {
        return BLE_ATT_ERR_UNLIKELY;
    }

    // 2. The flag is a single byte boolean.
    if (OS_MBUF_PKTLEN(ctxt->om) != sizeof(uint8_t))
    {
        return BLE_ATT_ERR_INVALID_ATTR_VALUE_LEN;
    }

    uint8_t revoke = 0;
    os_mbuf_copydata(ctxt->om, 0, sizeof(revoke), &revoke);

    if (revoke > 1)
    {
        ESP_LOGE(LOG_TAG, "Invalid value for Revoke: 0x%02x", revoke);
        return BLE_ATT_ERR_VALUE_NOT_ALLOWED;
    }

    // 3. Only a set flag revokes the credentials. The write itself is not persisted, as it is a
    // command rather than a configuration value.
    if (revoke == 0)
    {
        return 0;
    }

    ESP_LOGI(LOG_TAG, "Revoking credentials");

    // 4. Delete the stored CSR. A missing one means the device was never enrolled, which is nothing
    // to report: revocation is about the state it leaves behind, not about what was there before.
    esp_err_t err = erase_data(CSR_NAMESPACE);

    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND)
    {
        ESP_LOGE(LOG_TAG, "Failed to delete CSR: %s", esp_err_to_name(err));
        return BLE_ATT_ERR_UNLIKELY;
    }

    // 5. Delete the certificate issued for that CSR, which the device must no longer present.
    err = device_credentials_delete_certificate();

    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to delete certificate: %s", esp_err_to_name(err));
        return BLE_ATT_ERR_UNLIKELY;
    }

    // 6. Wipe the private key they were bound to, so the revoked certificate stays unusable even if
    // a copy of it is installed again.
    err = device_credentials_delete_private_key();

    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to delete the device private key: %s", esp_err_to_name(err));
        return BLE_ATT_ERR_UNLIKELY;
    }

    ESP_LOGI(LOG_TAG, "Successfully revoked credentials");
    return 0;
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

// Documented default value (docs/device/ble/authentication/overview.md): one year expressed in
// seconds, stored as the raw ASCII numeric string the mobile app writes, without a terminator.
static constexpr char DEFAULT_EXPIRATION[] = "31536000";

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
        .access_cb = csr_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN,
        .val_handle = nullptr,
        .arg = nullptr,
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
        .access_cb = revoke_access_cb,
        .flags = BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_WRITE_AUTHEN,
        .val_handle = nullptr,
        .arg = nullptr,
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
            .default_value = DEFAULT_EXPIRATION,
            .default_len = sizeof(DEFAULT_EXPIRATION) - 1,
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
