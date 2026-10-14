#include "security.h"

#include <stdio.h>
#include <stdlib.h>

#include "esp_log.h"
#include "mbedtls/md.h"
#include "mbedtls/pk.h"
#include "mbedtls/x509_crt.h"
#include "mbedtls/x509_csr.h"
#include "nvs.h"
#include "storage.h"
#include "uuid.h"
#include "psa/crypto.h"
#include "psa/crypto_struct.h"

#define KEY_ID      PSA_KEY_ID_USER_MIN // Key ID for the private key used in the application.
#define KEY_SIZE    256

#define CSR_SUBJECT_MAX_LEN 64   // Fits "CN=" followed by the 36 character UUID string and the null terminator.
#define CSR_PEM_MAX_LEN     1024 // A PEM encoded P-256 CSR is around 480 bytes, so this leaves room to spare.

static const char *TAG = "[SECURITY]";

// CA certificates the device trusts. Loaded from the storage layer by security_init, and extended by security_add_ca_certificate.
static mbedtls_x509_crt global_ca_store;

/**
 *
 * @brief Generates a private key with the specified @code KEY_ID@endcode and stores it in the PSA key store.
 * If a key with the same ID already exists, it is deleted and replaced by the new key.
 *
 * Internal to the security layer: the key is only created through the certificate request flow,
 * so it always matches the certificate issued for it.
 *
 * The generated key is intended for use in mutual authentication and secure communication.
 * The key is generated with the following attributes:
 * - Type: EC key pair
 * - Curve: secp256r1
 * - Algorithm: ECDSA with SHA-256, in the same form mbedTLS uses to sign with the key
 * - Usage: Sign
 *
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t security_generate_private_key() {

    // 1. Delete the key of any previous request, so the new request is never signed with an old key
    const psa_status_t destroy_status = psa_destroy_key(mbedtls_svc_key_id_make(0, KEY_ID));

    // 1.1 Destroying a key that does not exist is not an error, so only other failures are reported
    if (destroy_status != PSA_SUCCESS && destroy_status != PSA_ERROR_INVALID_HANDLE) {
        ESP_LOGE(TAG, "Failed to delete previous key: %d", (int) destroy_status);
        return ESP_FAIL;
    }

    // 2. Set the attributes for the new key
    psa_key_attributes_t attributes = PSA_KEY_ATTRIBUTES_INIT;
    psa_set_key_lifetime(&attributes, PSA_KEY_LIFETIME_PERSISTENT);
    psa_set_key_id(&attributes, KEY_ID);
    psa_set_key_usage_flags(&attributes, PSA_KEY_USAGE_SIGN_HASH);
    psa_set_key_algorithm(&attributes, MBEDTLS_PK_ALG_ECDSA(PSA_ALG_SHA_256));
    psa_set_key_type(&attributes, PSA_KEY_TYPE_ECC_KEY_PAIR(PSA_ECC_FAMILY_SECP_R1));
    psa_set_key_bits(&attributes, KEY_SIZE);

    // 3. Generate the new key
    psa_key_id_t key_id;
    const psa_status_t status = psa_generate_key(&attributes, &key_id);
    psa_reset_key_attributes(&attributes);

    // 3.1 If key generation fails, log the error and return ESP_FAIL
    if (status != PSA_SUCCESS) {
        ESP_LOGE(TAG, "Failed to generate key: %d", (int) status);
        return ESP_FAIL;
    }

    // 3.2 Check if the generated key ID matches the expected ID
    if (key_id != KEY_ID) {
        ESP_LOGE(TAG, "Generated key ID does not match expected ID: %d", (int) key_id);
        return ESP_FAIL;
    }

    // 3.3 Log the successful key generation and return ESP_OK
    ESP_LOGI(TAG, "Key generated and stored with ID: %d", KEY_ID);
    return ESP_OK;
}

/**
 *
 * @brief Signs a Certificate Signing Request with the device key and writes it in PEM format.
 *
 * The device key is wrapped from the PSA key store, so it signs the request without leaving the key store.
 *
 * @param subject the subject of the request in X.509 notation, such as "CN=<identity>". Must be null-terminated.
 * @param pem the buffer that receives the null-terminated PEM request.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t security_write_csr_pem(const char *subject, unsigned char *pem) {

    // 1. Wrap the device key from the PSA key store
    mbedtls_pk_context key;
    mbedtls_pk_init(&key);
    int ret = mbedtls_pk_wrap_psa(&key, mbedtls_svc_key_id_make(0, KEY_ID));
    if (ret != 0) {
        ESP_LOGE(TAG, "Failed to wrap private key: %d", ret);
        mbedtls_pk_free(&key);
        return ESP_FAIL;
    }

    // 2. Set the subject, the public key and the signature hash of the request
    mbedtls_x509write_csr req;
    mbedtls_x509write_csr_init(&req);
    ret = mbedtls_x509write_csr_set_subject_name(&req, subject);
    if (ret == 0) {
        mbedtls_x509write_csr_set_key(&req, &key);
        mbedtls_x509write_csr_set_md_alg(&req, MBEDTLS_MD_SHA256);

        // 3. Sign the request and write it in PEM format
        ret = mbedtls_x509write_csr_pem(&req, pem, CSR_PEM_MAX_LEN);
    }

    // 4. Release the request and the key wrapper. The PSA key itself stays in the key store.
    mbedtls_x509write_csr_free(&req);
    mbedtls_pk_free(&key);

    // 4.1 If any step failed, log the error and return ESP_FAIL
    if (ret != 0) {
        ESP_LOGE(TAG, "Failed to write CSR: %d", ret);
        return ESP_FAIL;
    }

    return ESP_OK;
}

/**
 *
 * @brief Installs every CA certificate stored in the storage layer into the global CA store.
 *
 * The global store is rebuilt from scratch, so calling this again does not install a certificate twice.
 * A missing CA list is not an error, it just leaves the store empty.
 *
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
static esp_err_t load_ca_store() {

    // 1. Rebuild the global store from scratch
    mbedtls_x509_crt_free(&global_ca_store);
    mbedtls_x509_crt_init(&global_ca_store);

    // 2. Check whether a CA list is stored. The size includes the null terminator.
    size_t stored_size = 0;
    esp_err_t err = storage_get_str_size(&ca_ctx, &stored_size);

    // 2.1 If no CA list is stored, the store stays empty
    if (err == ESP_ERR_NVS_NOT_FOUND) {
        return ESP_OK;
    }

    // 2.2 If there was an error checking for the CA list, log the error and return the error
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to check CA store: %s", esp_err_to_name(err));
        return err;
    }

    // 3. Read the stored CA list
    char *stored = malloc(stored_size);
    if (stored == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate memory for CA store");
        return ESP_ERR_NO_MEM;
    }

    err = storage_get_str(&ca_ctx, stored, stored_size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to read CA store: %s", esp_err_to_name(err));
        free(stored);
        return err;
    }

    // 4. Parse every certificate in the list into the global store. A non-zero result means at least one failed.
    const int ret = mbedtls_x509_crt_parse(&global_ca_store, (const unsigned char *) stored, stored_size);
    free(stored);

    if (ret != 0) {
        ESP_LOGE(TAG, "Failed to install stored CA certificates: %d", ret);
        return ESP_FAIL;
    }

    ESP_LOGI(TAG, "Stored CA certificates installed");
    return ESP_OK;
}

esp_err_t security_init() {

    ESP_LOGI(TAG, "Initializing security");

    // 1. Initialize the PSA Crypto library
    const psa_status_t status = psa_crypto_init();
    if (status != PSA_SUCCESS) {
        ESP_LOGE(TAG, "Failed to initialize crypto library: %d", (int) status);
        return ESP_FAIL;
    }

    // 2. Allocate memory for the device identity
    uint8_t *device_identity = malloc(sizeof(uint8_t) * UUID_LEN);
    if (device_identity == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate memory for device identity");
        return ESP_ERR_NO_MEM;
    }

    // 3. Try to read the device identity from storage
    esp_err_t err = storage_get_blob(&identity_ctx, device_identity, UUID_LEN);

    // 4. If the identity does not exist, generate a new UUIDv4 and store it
    if (err == ESP_ERR_NVS_NOT_FOUND) {

        ESP_LOGI(TAG, "Device identity not found, generating a new UUIDv4");


        // 4.1 Generate a new UUIDv4
        err = uuid_generate_v4(device_identity);

        // 4.1.1 If the UUID generation fails, free the allocated memory and return the error
        if (err != ESP_OK) {
            ESP_LOGE(TAG, "Failed to generate UUIDv4: %s", esp_err_to_name(err));
            free(device_identity);
            return err;
        }

        // 4.2 Store the new UUIDv4 in storage
        err = storage_set_blob(&identity_ctx, device_identity, UUID_LEN);

        // 4.2.1 If storing the UUID fails, free the allocated memory and return the error
        if (err != ESP_OK) {
            free(device_identity);
            ESP_LOGE(TAG, "Failed to store UUIDv4: %s", esp_err_to_name(err));
            return err;
        }

    }

    // 5. If reading the identity from storage fails for any reason, free the allocated memory and return the error
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get UUIDv4: %s", esp_err_to_name(err));
        free(device_identity);
        return err;
    }

    // 6. Install every stored CA certificate into the global store
    err = load_ca_store();
    if (err != ESP_OK) {
        free(device_identity);
        return err;
    }

    // 7. If we reach this point, the device identity is valid, so we can free the allocated memory and return ESP_OK
    ESP_LOGI(TAG, "Security initialized.");
    free(device_identity);
    return ESP_OK;
}

esp_err_t security_get_identity(uint8_t *identity) {

    // 1. Validate the input buffer
    if (identity == NULL) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Read the device identity from storage
    const esp_err_t err = storage_get_blob(&identity_ctx, identity, UUID_LEN);
    if (err != ESP_OK) {
        return err;
    }

    return ESP_OK;
}

esp_err_t security_get_private_key(psa_key_id_t *key_id) {

    // 1. Validate the input pointer
    if (key_id == NULL) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Check if the key already exists in the PSA key store
    psa_key_attributes_t attributes = PSA_KEY_ATTRIBUTES_INIT;
    const psa_status_t status = psa_get_key_attributes(KEY_ID, &attributes);

    // 2.1 If the key exists, return ESP_OK and set the key_id to the existing key's ID
    if (status == PSA_SUCCESS) {
        psa_reset_key_attributes(&attributes);
        *key_id = KEY_ID;
        return ESP_OK;
    }

    // 2.2 If the key does not exist, return NOT_FOUND error. PSA reports a missing key as an invalid handle.
    if (status == PSA_ERROR_INVALID_HANDLE) {
        return ESP_ERR_NOT_FOUND;
    }

    // 2.3 If there was an error retrieving the key attributes, log the error and return ESP_FAIL
    ESP_LOGE(TAG, "Failed to get key attributes: %d", (int) status);
    return ESP_FAIL;
}

esp_err_t security_create_csr() {

    // 1. Check if a CSR already exists in storage
    size_t csr_size;
    esp_err_t err = storage_get_str_size(&csr_ctx, &csr_size);

    // 1.1 If a CSR already exists, log the information and return ESP_ERR_NVS_INVALID_STATE
    if (err == ESP_OK) {
        ESP_LOGI(TAG, "CSR already exists, skipping generation");
        return ESP_ERR_NVS_INVALID_STATE;
    }

    // 1.2 If there was an error checking for the CSR, log the error and return the error
    if (err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to check CSR existence: %s", esp_err_to_name(err));
        return err;
    }

    // 2. Check if a certificate already exists in storage
    size_t cert_size;
    err = storage_get_str_size(&cert_ctx, &cert_size);

    // 2.1 If a certificate already exists, log the information and return ESP_ERR_NVS_INVALID_STATE
    if (err == ESP_OK) {
        ESP_LOGI(TAG, "Certificate already exists, skipping CSR generation");
        return ESP_ERR_NVS_INVALID_STATE;
    }

    // 2.2 If there was an error checking for the certificate, log the error and return the error
    if (err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to check certificate existence: %s", esp_err_to_name(err));
        return err;
    }

    // 3. Read the device identity, which is used as the common name of the request
    uint8_t identity[UUID_LEN];
    err = security_get_identity(identity);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get UUIDv4: %s", esp_err_to_name(err));
        return err;
    }

    char *uuid = uuid_to_string(identity);
    if (uuid == nullptr) {
        ESP_LOGE(TAG, "Failed to convert UUIDv4 to string");
        return ESP_FAIL;
    }

    char subject[CSR_SUBJECT_MAX_LEN];
    const int subject_len = snprintf(subject, sizeof(subject), "CN=%s", uuid);
    free(uuid);

    if (subject_len < 0 || subject_len >= (int) sizeof(subject)) {
        ESP_LOGE(TAG, "Failed to build CSR subject");
        return ESP_FAIL;
    }

    // 4. Generate a new private key, replacing the key of any previous request
    err = security_generate_private_key();
    if (err != ESP_OK) {
        return err;
    }

    // 5. Sign the request with the device key and write it in PEM format
    unsigned char *pem = malloc(CSR_PEM_MAX_LEN);
    if (pem == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate memory for CSR");
        return ESP_ERR_NO_MEM;
    }

    err = security_write_csr_pem(subject, pem);
    if (err != ESP_OK) {
        free(pem);
        return err;
    }

    // 6. Store the request, so every read returns the same one
    err = storage_set_str(&csr_ctx, (const char *) pem);
    free(pem);

    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to store CSR: %s", esp_err_to_name(err));
        return err;
    }

    ESP_LOGI(TAG, "CSR generated and stored");
    return ESP_OK;
}

char *security_get_csr() {

    // 1. Check if the CSR exists in storage
    size_t csr_size;
    esp_err_t err = storage_get_str_size(&csr_ctx, &csr_size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get CSR size: %s", esp_err_to_name(err));
        return nullptr;
    }

    // 2. Allocate memory for the CSR
    char *csr = malloc(csr_size);
    if (csr == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate memory for CSR");
        return nullptr;
    }

    // 3. Read the CSR from storage
    err = storage_get_str(&csr_ctx, csr, csr_size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get CSR from storage: %s", esp_err_to_name(err));
        free(csr);
        return nullptr;
    }

    return csr;
}

char *security_get_certificate() {

    // 1. Check if the certificate exists in storage
    size_t cert_size;
    esp_err_t err = storage_get_str_size(&cert_ctx, &cert_size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get certificate size: %s", esp_err_to_name(err));
        return nullptr;
    }

    // 2. Allocate memory for the certificate
    char *cert = malloc(cert_size);
    if (cert == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate memory for certificate");
        return nullptr;
    }

    // 3. Read the certificate from storage
    err = storage_get_str(&cert_ctx, cert, cert_size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get certificate from storage: %s", esp_err_to_name(err));
        free(cert);
        return nullptr;
    }

    return cert;
}

esp_err_t security_install_certificate(const char *certificate) {

    // 1. Validate the input
    if (certificate == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Store the certificate first, so an interruption leaves the request in place
    esp_err_t err = storage_set_str(&cert_ctx, certificate);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to store certificate: %s", esp_err_to_name(err));
        return err;
    }

    // 3. Delete the stored request, which is no longer needed once a certificate is installed
    err = storage_erase(&csr_ctx);

    // 3.1 A request that is not stored is not an error, so only other failures are reported
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to delete CSR: %s", esp_err_to_name(err));
        return err;
    }

    // 4. Log the installation and return ESP_OK
    ESP_LOGI(TAG, "Certificate installed");
    return ESP_OK;
}

esp_err_t security_add_ca_certificate(const char *ca_certificate) {

    // 1. Validate the input
    if (ca_certificate == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Check that the certificate is valid PEM before it is stored. The length includes the null terminator.
    mbedtls_x509_crt parsed;
    mbedtls_x509_crt_init(&parsed);
    const int ret = mbedtls_x509_crt_parse(&parsed, (const unsigned char *) ca_certificate, strlen(ca_certificate) + 1);
    mbedtls_x509_crt_free(&parsed);

    // 2.1 If the certificate is not valid PEM, log the error and return ESP_ERR_INVALID_ARG
    if (ret != 0) {
        ESP_LOGE(TAG, "Failed to parse CA certificate: %d", ret);
        return ESP_ERR_INVALID_ARG;
    }

    // 3. Read the CA certificates already stored. The size includes the null terminator, and zero means none are stored.
    size_t stored_size = 0;
    esp_err_t err = storage_get_str_size(&ca_ctx, &stored_size);

    // 3.1 If there was an error checking for the store, log the error and return the error
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to check CA store: %s", esp_err_to_name(err));
        return err;
    }

    if (err == ESP_ERR_NVS_NOT_FOUND) {
        stored_size = 0;
    }

    // 4. Allocate room for the stored certificates, the new one and the null terminator
    const size_t new_len = strlen(ca_certificate);
    char *ca_list = malloc(stored_size + new_len + 1);
    if (ca_list == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate memory for CA store");
        return ESP_ERR_NO_MEM;
    }
    ca_list[0] = '\0';

    // 4.1 Read the stored certificates, so the new one is appended after them
    if (stored_size > 0) {
        err = storage_get_str(&ca_ctx, ca_list, stored_size);
        if (err != ESP_OK) {
            ESP_LOGE(TAG, "Failed to read CA store: %s", esp_err_to_name(err));
            free(ca_list);
            return err;
        }

        // 4.2 Separate the stored PEM blocks from the new one with a line break, if one is missing
        const size_t stored_len = strlen(ca_list);
        if (stored_len > 0 && ca_list[stored_len - 1] != '\n') {
            strcat(ca_list, "\n");
        }
    }

    // 4.3 Append the new certificate
    strcat(ca_list, ca_certificate);

    // 5. Store the updated list and free the buffer
    err = storage_set_str(&ca_ctx, ca_list);
    free(ca_list);

    // 5.1 If storing the list fails, log the error and return the error
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to store CA certificate: %s", esp_err_to_name(err));
        return err;
    }

    // 6. Install the certificate into the global store, so it is trusted without a reset
    if (mbedtls_x509_crt_parse(&global_ca_store, (const unsigned char *) ca_certificate, strlen(ca_certificate) + 1) != 0) {
        ESP_LOGE(TAG, "Failed to install CA certificate");
        return ESP_FAIL;
    }

    // 7. Log the addition and return ESP_OK
    ESP_LOGI(TAG, "CA certificate added");
    return ESP_OK;
}

const mbedtls_x509_crt *security_get_ca_store() {
    return &global_ca_store;
}

esp_err_t security_revoke() {

    // 1. Delete the certificate first, so an interruption cannot leave the device refusing every read
    esp_err_t err = storage_erase(&cert_ctx);

    // 1.1 A certificate that is not stored is not an error, so only other failures are reported
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to delete certificate: %s", esp_err_to_name(err));
        return err;
    }

    // 2. Delete the CSR
    err = storage_erase(&csr_ctx);

    // 2.1 A CSR that is not stored is not an error, so only other failures are reported
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to delete CSR: %s", esp_err_to_name(err));
        return err;
    }

    // 3. Delete the private key, which the certificate and the CSR were bound to
    const psa_status_t destroy_status = psa_destroy_key(mbedtls_svc_key_id_make(0, KEY_ID));

    // 3.1 A key that does not exist is not an error, so only other failures are reported
    if (destroy_status != PSA_SUCCESS && destroy_status != PSA_ERROR_INVALID_HANDLE) {
        ESP_LOGE(TAG, "Failed to delete private key: %d", (int) destroy_status);
        return ESP_FAIL;
    }

    // 4. Log the successful revocation of credentials and return ESP_OK
    ESP_LOGI(TAG, "Credentials revoked");
    return ESP_OK;
}