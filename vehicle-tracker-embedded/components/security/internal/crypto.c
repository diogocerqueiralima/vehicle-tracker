#include "crypto.h"

#include <stdio.h>
#include "esp_log.h"
#include "mbedtls/pk.h"
#include <string.h>
#include "mbedtls/x509_crt.h"
#include "mbedtls/x509_csr.h"

#define TAG "crypto"

/**
 * @brief Size in bits of the keys, which are ECDSA P-256.
 */
#define KEY_BITS 256

/**
 * @brief Maximum size in bytes of the CSR subject, including the "CN=" prefix and the null terminator.
 */
#define SUBJECT_MAX_SIZE 128

/**
 * @brief Size in bytes of a buffer able to hold the SubjectPublicKeyInfo DER of an ECDSA P-256 public key (91 bytes).
 */
#define PUBLIC_KEY_DER_MAX_SIZE 128

esp_err_t crypto_generate_private_key(const psa_key_id_t key_id) {

    // 1. Make sure the PSA crypto subsystem is ready
    if (psa_crypto_init() != PSA_SUCCESS) {
        ESP_LOGE(TAG, "Failed to initialize PSA crypto");
        return ESP_FAIL;
    }

    // 2. Destroy the old private key if one already exists, so it is overridden
    const psa_status_t destroy_status = psa_destroy_key(key_id);
    if (destroy_status != PSA_SUCCESS && destroy_status != PSA_ERROR_INVALID_HANDLE) {
        ESP_LOGE(TAG, "Failed to destroy old private key");
        return ESP_FAIL;
    }

    // 3. Describe the key: persistent ECDSA P-256 key pair, usable only for signing
    psa_key_attributes_t attributes = PSA_KEY_ATTRIBUTES_INIT;
    psa_set_key_id(&attributes, key_id);
    psa_set_key_lifetime(&attributes, PSA_KEY_LIFETIME_PERSISTENT);
    psa_set_key_type(&attributes, PSA_KEY_TYPE_ECC_KEY_PAIR(PSA_ECC_FAMILY_SECP_R1));
    psa_set_key_bits(&attributes, KEY_BITS);
    psa_set_key_usage_flags(&attributes, PSA_KEY_USAGE_SIGN_HASH | PSA_KEY_USAGE_SIGN_MESSAGE);
    psa_set_key_algorithm(&attributes, PSA_ALG_ECDSA(PSA_ALG_SHA_256));

    // 4. Generate the key
    psa_key_id_t generated_id = PSA_KEY_ID_NULL;
    const psa_status_t generate_status = psa_generate_key(&attributes, &generated_id);
    psa_reset_key_attributes(&attributes);
    if (generate_status != PSA_SUCCESS) {
        ESP_LOGE(TAG, "Failed to generate private key");
        return ESP_FAIL;
    }

    return ESP_OK;
}

esp_err_t crypto_generate_csr(const psa_key_id_t key_id, const char *common_name, char *out_pem, const size_t size) {

    // 1. Validate the arguments
    if (common_name == nullptr || out_pem == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Build the subject
    char subject[SUBJECT_MAX_SIZE];
    const int written = snprintf(subject, sizeof(subject), "CN=%s", common_name);
    if (written < 0 || (size_t) written >= sizeof(subject)) {
        ESP_LOGE(TAG, "Common name is too long");
        return ESP_ERR_INVALID_ARG;
    }

    // 3. Wrap the PSA private key, so it can be used to sign the CSR
    mbedtls_pk_context pk;
    mbedtls_pk_init(&pk);
    if (mbedtls_pk_wrap_psa(&pk, key_id) != 0) {
        ESP_LOGE(TAG, "Failed to wrap private key");
        mbedtls_pk_free(&pk);
        return ESP_FAIL;
    }

    // 4. Configure the CSR
    mbedtls_x509write_csr csr;
    mbedtls_x509write_csr_init(&csr);
    mbedtls_x509write_csr_set_key(&csr, &pk);
    mbedtls_x509write_csr_set_md_alg(&csr, MBEDTLS_MD_SHA256);

    esp_err_t err = ESP_FAIL;
    if (mbedtls_x509write_csr_set_subject_name(&csr, subject) != 0) {
        ESP_LOGE(TAG, "Failed to set CSR subject");
        goto cleanup;
    }

    // 5. Sign it and write it as PEM
    if (mbedtls_x509write_csr_pem(&csr, (unsigned char *) out_pem, size) != 0) {
        ESP_LOGE(TAG, "Failed to write CSR");
        goto cleanup;
    }

    err = ESP_OK;

cleanup:
    mbedtls_x509write_csr_free(&csr);
    mbedtls_pk_free(&pk);
    return err;
}

esp_err_t crypto_parse_certificate(const char *cert_pem, mbedtls_x509_crt *out_crt) {

    // 1. Validate the arguments
    if (cert_pem == nullptr || out_crt == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Parse the certificate, the length must include the null terminator for PEM data
    mbedtls_x509_crt_init(out_crt);
    if (mbedtls_x509_crt_parse(out_crt, (const unsigned char *) cert_pem, strlen(cert_pem) + 1) != 0) {
        ESP_LOGE(TAG, "Failed to parse certificate");
        mbedtls_x509_crt_free(out_crt);
        return ESP_ERR_INVALID_ARG;
    }

    return ESP_OK;
}

esp_err_t crypto_certificate_matches_key(const mbedtls_x509_crt *crt, const psa_key_id_t key_id, bool *out_matches) {

    // 1. Validate the arguments
    if (crt == nullptr || out_matches == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Wrap the PSA private key, so its public key can be written
    mbedtls_pk_context pk;
    mbedtls_pk_init(&pk);
    esp_err_t err = ESP_FAIL;
    if (mbedtls_pk_wrap_psa(&pk, key_id) != 0) {
        ESP_LOGE(TAG, "Failed to wrap private key");
        goto cleanup;
    }

    // 3. Write both public keys as DER. The data is written at the end of the buffers
    unsigned char key_der[PUBLIC_KEY_DER_MAX_SIZE];
    const int key_len = mbedtls_pk_write_pubkey_der(&pk, key_der, sizeof(key_der));
    if (key_len < 0) {
        ESP_LOGE(TAG, "Failed to write public key of the private key");
        goto cleanup;
    }

    unsigned char crt_der[PUBLIC_KEY_DER_MAX_SIZE];
    const int crt_len = mbedtls_pk_write_pubkey_der(&crt->pk, crt_der, sizeof(crt_der));
    if (crt_len < 0) {
        ESP_LOGE(TAG, "Failed to write public key of the certificate");
        goto cleanup;
    }

    // 4. Compare them, the DER is written at the end of the buffers, so we compare the last key_len bytes of both
    *out_matches = key_len == crt_len &&
                   memcmp(key_der + sizeof(key_der) - key_len, crt_der + sizeof(crt_der) - crt_len, key_len) == 0;
    err = ESP_OK;

cleanup:
    mbedtls_pk_free(&pk);
    return err;
}

esp_err_t crypto_certificate_is_valid(const mbedtls_x509_crt *crt, bool *out_valid) {

    // 1. Validate the arguments
    if (crt == nullptr || out_valid == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Valid when the validity period already started and did not end yet
    *out_valid = !mbedtls_x509_time_is_future(&crt->valid_from) && !mbedtls_x509_time_is_past(&crt->valid_to);

    return ESP_OK;
}
