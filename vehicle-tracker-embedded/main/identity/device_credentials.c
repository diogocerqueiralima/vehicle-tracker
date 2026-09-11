#include "device_credentials.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "esp_log.h"
#include "ble/services/authentication_service.h"
#include "mbedtls/pk.h"
#include "mbedtls/x509_csr.h"
#include "identity/device_identity.h"
#include "storage/storage.h"

#define LOG_TAG "DEVICE_CREDENTIALS"

// Identifier of the device's key pair inside PSA's persistent key store. PSA keeps the key material
// in NVS and hands out only this reference, so the private key never reaches an application buffer.
#define DEVICE_CREDENTIALS_PRIVATE_KEY_ID ((psa_key_id_t) 1)

// Hash the CSR is signed over. Both defines must name the same hash: the key policy fixes what PSA
// is allowed to sign, and PSA refuses the signature when the request asks for a different hash.
#define DEVICE_CREDENTIALS_CSR_MD MBEDTLS_MD_SHA512
#define DEVICE_CREDENTIALS_KEY_ALG MBEDTLS_PK_ALG_ECDSA(PSA_ALG_SHA_512)

// The key is a NIST P-256 (secp256r1) signing key, which is 256 bits long.
#define DEVICE_CREDENTIALS_KEY_SIZE_BITS 256

// Room for a PEM-encoded P-256 CSR carrying a UUID common name, which runs to roughly 450 bytes.
#define DEVICE_CREDENTIALS_CSR_PEM_LEN 1024

/**
 * @brief Convert a PSA status code to an ESP-IDF error code.
 *
 * @param status the PSA status code to convert
 * @return the corresponding ESP-IDF error code
 */
static esp_err_t psa_status_to_esp_err(const psa_status_t status) {
    switch (status) {
        case PSA_SUCCESS:
            return ESP_OK;
        case PSA_ERROR_INVALID_ARGUMENT:
            return ESP_ERR_INVALID_ARG;
        case PSA_ERROR_INSUFFICIENT_MEMORY:
            return ESP_ERR_NO_MEM;
        case PSA_ERROR_DOES_NOT_EXIST:
            return ESP_ERR_NOT_FOUND;
        case MBEDTLS_ERR_PK_FEATURE_UNAVAILABLE:
            return ESP_ERR_NOT_SUPPORTED;
        default:
            return ESP_FAIL;
    }
}

// Writes a PEM-encoded CSR for the given subject into out_pem, signed by the referenced PSA key.
static esp_err_t write_csr_pem(
    const psa_key_id_t key_id, const char *subject, char *out_pem, const size_t pem_len
) {

    mbedtls_pk_context key;
    mbedtls_x509write_csr csr;

    mbedtls_pk_init(&key);
    mbedtls_x509write_csr_init(&csr);

    // 1. Wrap the key identifier in a PK context: the signature is computed inside PSA, and the
    // private key is never copied out of the key store
    int ret = mbedtls_pk_wrap_psa(&key, key_id);

    if (ret != 0) {
        ESP_LOGE(LOG_TAG, "Failed to wrap PSA key: %d", ret);
        mbedtls_pk_free(&key);
        mbedtls_x509write_csr_free(&csr);
        return psa_status_to_esp_err(ret);
    }

    // 2. Describe the request: an ECDSA signature over a subject naming this device
    mbedtls_x509write_csr_set_md_alg(&csr, DEVICE_CREDENTIALS_CSR_MD);
    mbedtls_x509write_csr_set_key(&csr, &key);
    ret = mbedtls_x509write_csr_set_subject_name(&csr, subject);

    if (ret != 0) {
        ESP_LOGE(LOG_TAG, "Failed to set subject name: %d", ret);
        mbedtls_pk_free(&key);
        mbedtls_x509write_csr_free(&csr);
        return psa_status_to_esp_err(ret);
    }

    // 3. Ask for the key usage a device certificate needs to authenticate itself
    ret = mbedtls_x509write_csr_set_key_usage(&csr, MBEDTLS_X509_KU_DIGITAL_SIGNATURE);

    if (ret != 0) {
        ESP_LOGE(LOG_TAG, "Failed to set key usage: %d", ret);
        mbedtls_pk_free(&key);
        mbedtls_x509write_csr_free(&csr);
        return psa_status_to_esp_err(ret);
    }

    // 4. Serialize the request, signing it through the wrapped key
    ret = mbedtls_x509write_csr_pem(&csr, (unsigned char *) out_pem, pem_len);

    if (ret != 0) {
        ESP_LOGE(LOG_TAG, "Failed to serialize CSR: %d", ret);
        mbedtls_pk_free(&key);
        mbedtls_x509write_csr_free(&csr);
        return psa_status_to_esp_err(ret);
    }

    mbedtls_x509write_csr_free(&csr);
    mbedtls_pk_free(&key);

    return ESP_OK;
}

esp_err_t device_credentials_get_private_key(psa_key_id_t *out_key_id) {

    // 1. Validate the output pointer
    if (out_key_id == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Bring up PSA, which loads its persistent key store from NVS
    psa_status_t status = psa_crypto_init();
    if (status != PSA_SUCCESS) {
        return psa_status_to_esp_err(status);
    }

    // 3. Reuse the stored key whenever this is not the first boot
    psa_key_attributes_t attributes = PSA_KEY_ATTRIBUTES_INIT;
    status = psa_get_key_attributes(DEVICE_CREDENTIALS_PRIVATE_KEY_ID, &attributes);

    if (status == PSA_SUCCESS) {
        *out_key_id = DEVICE_CREDENTIALS_PRIVATE_KEY_ID;
        return ESP_OK;
    }

    // 4. Only a missing key means first boot. Any other failure is a fault in the key store, and
    // generating over it would just collide with the key that is already there. PSA reports an
    // absent persistent key as PSA_ERROR_INVALID_HANDLE, not PSA_ERROR_DOES_NOT_EXIST
    if (status != PSA_ERROR_INVALID_HANDLE && status != PSA_ERROR_DOES_NOT_EXIST) {
        ESP_LOGE(LOG_TAG, "Failed to read the device private key: %ld", (long) status);
        return psa_status_to_esp_err(status);
    }

    // 5. First boot: a persistent NIST P-256 key that may sign but carries no export permission
    psa_set_key_type(&attributes, PSA_KEY_TYPE_ECC_KEY_PAIR(PSA_ECC_FAMILY_SECP_R1));
    psa_set_key_bits(&attributes, DEVICE_CREDENTIALS_KEY_SIZE_BITS);
    psa_set_key_usage_flags(&attributes, PSA_KEY_USAGE_SIGN_HASH);
    psa_set_key_algorithm(&attributes, DEVICE_CREDENTIALS_KEY_ALG);
    psa_set_key_lifetime(&attributes, PSA_KEY_LIFETIME_PERSISTENT);
    psa_set_key_id(&attributes, DEVICE_CREDENTIALS_PRIVATE_KEY_ID);

    // 6. Generate it; PSA writes the key material to its own store and returns just the identifier
    status = psa_generate_key(&attributes, out_key_id);

    return psa_status_to_esp_err(status);
}

char *device_credentials_generate_csr(esp_err_t *err) {

    // 1. Validate the output pointer
    if (err == nullptr) {
        return nullptr;
    }

    // 2. Look up, or create on first boot, the device's private key
    psa_key_id_t key_id = PSA_KEY_ID_NULL;
    esp_err_t error = device_credentials_get_private_key(&key_id);

    if (error != ESP_OK) {
        ESP_LOGE(LOG_TAG, "Failed to load device private key: %s", esp_err_to_name(error));
        *err = error;
        return nullptr;
    }

    // 3. Name the subject after the identifier the device shows as a QR code for registration
    uint8_t device_id[DEVICE_IDENTITY_ID_LEN];
    error = device_identity_get(device_id);

    if (error != ESP_OK) {
        ESP_LOGE(LOG_TAG, "Failed to load device identity: %s", esp_err_to_name(error));
        *err = error;
        return nullptr;
    }

    char device_id_str[DEVICE_IDENTITY_STRING_LEN + 1];
    device_identity_to_string(device_id, device_id_str);

    char subject[sizeof("CN=, O=MyTracker, C=PT") + DEVICE_IDENTITY_STRING_LEN];
    snprintf(subject, sizeof(subject), "CN=%s, O=MyTracker, C=PT", device_id_str);

    // 4. Write the request into a buffer the caller takes ownership of
    char *pem = malloc(DEVICE_CREDENTIALS_CSR_PEM_LEN);
    if (pem == nullptr) {
        ESP_LOGE(LOG_TAG, "Failed to allocate memory for device credentials pem");
        *err = ESP_ERR_NO_MEM;
        return nullptr;
    }

    // 5. Generate the CSR and write it into the buffer, signing it with the device's private key
    error = write_csr_pem(key_id, subject, pem, DEVICE_CREDENTIALS_CSR_PEM_LEN);
    if (error != ESP_OK) {
        ESP_LOGE(LOG_TAG, "Failed to write device credentials csr pem: %s", esp_err_to_name(error));
        free(pem);
        *err = error;
        return nullptr;
    }

    // 6. Hand back the PEM; it is null-terminated, so the caller can measure it with strlen()
    *err = ESP_OK;
    return pem;
}

esp_err_t device_credentials_save_certificate(const char *pem) {

    // 1. Validate the input pointer
    if (pem == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Persist the certificate in NVS so it survives reboots
    return save_data(CERTIFICATE_NAMESPACE, pem, strlen(pem));
}

char *device_credentials_load_certificate(esp_err_t *err) {

    if (err == nullptr) {
        return nullptr;
    }

    // 1. Ask how much was stored, which also tells whether the device is enrolled at all
    size_t stored_len = 0;
    esp_err_t error = get_data_size(CERTIFICATE_NAMESPACE, &stored_len);

    if (error != ESP_OK) {
        *err = ESP_ERR_NOT_FOUND;
        return nullptr;
    }

    // 2. Read it into a buffer the caller takes ownership of, with room for a terminating null byte
    char *pem = malloc(stored_len + 1);
    if (pem == nullptr) {
        ESP_LOGE(LOG_TAG, "Failed to allocate memory for device credentials pem");
        *err = ESP_ERR_NO_MEM;
        return nullptr;
    }

    // 3. Load the certificate from storage, which is public and can be read into an application buffer
    error = load_data(CERTIFICATE_NAMESPACE, pem, stored_len);
    if (error != ESP_OK) {
        ESP_LOGE(LOG_TAG, "Failed to load device credentials pem: %s", esp_err_to_name(error));
        free(pem);
        *err = error;
        return nullptr;
    }

    // 4. Null-terminate the string so the caller can measure it with strlen()
    pem[stored_len] = '\0';
    *err = ESP_OK;
    return pem;
}
