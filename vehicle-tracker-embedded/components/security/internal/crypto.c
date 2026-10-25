#include "crypto.h"

#include <stdio.h>
#include "esp_log.h"
#include "mbedtls/pk.h"
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

    if (common_name == nullptr || out_pem == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 1. Build the subject
    char subject[SUBJECT_MAX_SIZE];
    const int written = snprintf(subject, sizeof(subject), "CN=%s", common_name);
    if (written < 0 || (size_t) written >= sizeof(subject)) {
        ESP_LOGE(TAG, "Common name is too long");
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Wrap the PSA private key, so it can be used to sign the CSR
    mbedtls_pk_context pk;
    mbedtls_pk_init(&pk);
    if (mbedtls_pk_wrap_psa(&pk, key_id) != 0) {
        ESP_LOGE(TAG, "Failed to wrap private key");
        mbedtls_pk_free(&pk);
        return ESP_FAIL;
    }

    // 3. Configure the CSR
    mbedtls_x509write_csr csr;
    mbedtls_x509write_csr_init(&csr);
    mbedtls_x509write_csr_set_key(&csr, &pk);
    mbedtls_x509write_csr_set_md_alg(&csr, MBEDTLS_MD_SHA256);

    esp_err_t err = ESP_FAIL;
    if (mbedtls_x509write_csr_set_subject_name(&csr, subject) != 0) {
        ESP_LOGE(TAG, "Failed to set CSR subject");
        goto cleanup;
    }

    // 4. Sign it and write it as PEM
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
