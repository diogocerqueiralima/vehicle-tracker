#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "esp_err.h"
#include "mbedtls/x509_csr.h"
#include "nvs.h"
#include "psa/crypto.h"
#include "unity.h"
#include "security.h"
#include "storage.h"
#include "uuid.h"

#define TEST_TAG "[security]"
#define PEM_HEADER "-----BEGIN CERTIFICATE REQUEST-----"
#define TEST_CERT "test-certificate"
#define SUBJECT_MAX_LEN 64

/**
 * Parses the PEM request and checks that its subject matches the expected one.
 */
static void assert_csr_subject(const char *pem, const char *expected_subject) {
    mbedtls_x509_csr csr;
    mbedtls_x509_csr_init(&csr);

    // The length includes the null terminator, as mbedTLS expects for PEM input
    TEST_ASSERT_EQUAL(0, mbedtls_x509_csr_parse(&csr, (const unsigned char *) pem, strlen(pem) + 1));

    char subject[SUBJECT_MAX_LEN];
    TEST_ASSERT_GREATER_THAN(0, mbedtls_x509_dn_gets(subject, sizeof(subject), &csr.subject));
    TEST_ASSERT_EQUAL_STRING(expected_subject, subject);

    mbedtls_x509_csr_free(&csr);
}

// --- security_init ---

TEST_CASE("init keeps the existing identity and creates one when it is missing", TEST_TAG) {
    uint8_t first[UUID_LEN];
    uint8_t second[UUID_LEN];
    TEST_ASSERT_EQUAL(ESP_OK, security_get_identity(first));

    // Calling init again must not replace the identity
    TEST_ASSERT_EQUAL(ESP_OK, security_init());
    TEST_ASSERT_EQUAL(ESP_OK, security_get_identity(second));
    TEST_ASSERT_EQUAL_UINT8_ARRAY(first, second, UUID_LEN);

    // Once the identity is removed, init generates a new one
    TEST_ASSERT_EQUAL(ESP_OK, storage_erase(&identity_ctx));
    TEST_ASSERT_EQUAL(ESP_OK, security_init());
    TEST_ASSERT_EQUAL(ESP_OK, security_get_identity(second));
    TEST_ASSERT_NOT_EQUAL(0, memcmp(first, second, UUID_LEN));
}

// --- security_get_identity ---

TEST_CASE("get identity returns the stored identity and rejects a null buffer", TEST_TAG) {
    uint8_t identity[UUID_LEN];

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_get_identity(nullptr));
    TEST_ASSERT_EQUAL(ESP_OK, security_get_identity(identity));

    TEST_ASSERT_EQUAL(ESP_OK, storage_erase(&identity_ctx));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, security_get_identity(identity));
}

// --- security_get_private_key ---

TEST_CASE("get private key reports not found until the request creates it", TEST_TAG) {
    psa_key_id_t key_id = PSA_KEY_ID_NULL;

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_get_private_key(nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_get_private_key(&key_id));

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_OK, security_get_private_key(&key_id));
    TEST_ASSERT_EQUAL(PSA_KEY_ID_USER_MIN, key_id);
}

// --- security_create_csr ---

TEST_CASE("create csr stores a PEM request with the identity as common name", TEST_TAG) {
    uint8_t identity[UUID_LEN];
    TEST_ASSERT_EQUAL(ESP_OK, security_get_identity(identity));

    char *uuid = uuid_to_string(identity);
    TEST_ASSERT_NOT_NULL(uuid);
    char expected_subject[SUBJECT_MAX_LEN];
    snprintf(expected_subject, sizeof(expected_subject), "CN=%s", uuid);
    free(uuid);

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());

    char *csr = security_get_csr();
    TEST_ASSERT_NOT_NULL(csr);
    TEST_ASSERT_NOT_NULL(strstr(csr, PEM_HEADER));
    assert_csr_subject(csr, expected_subject);
    free(csr);
}

TEST_CASE("create csr fails when a CSR or a certificate is already stored", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_INVALID_STATE, security_create_csr());

    TEST_ASSERT_EQUAL(ESP_OK, storage_erase(&csr_ctx));
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, TEST_CERT));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_INVALID_STATE, security_create_csr());
}

// --- security_get_csr ---

TEST_CASE("get csr returns null until a request is created, then the stored PEM", TEST_TAG) {
    TEST_ASSERT_NULL(security_get_csr());

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());

    char *csr = security_get_csr();
    TEST_ASSERT_NOT_NULL(csr);

    char *stored_csr = malloc(strlen(csr) + 1);
    TEST_ASSERT_NOT_NULL(stored_csr);
    size_t stored_len = strlen(csr) + 1;
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_str(&csr_ctx, stored_csr, stored_len));
    TEST_ASSERT_EQUAL_STRING(stored_csr, csr);

    free(stored_csr);
    free(csr);
}

// --- security_revoke ---

TEST_CASE("revoke deletes the certificate, the CSR and the private key", TEST_TAG) {
    psa_key_id_t key_id = PSA_KEY_ID_NULL;

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, TEST_CERT));

    TEST_ASSERT_EQUAL(ESP_OK, security_revoke());

    size_t size = 0;
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_str_size(&cert_ctx, &size));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_str_size(&csr_ctx, &size));
    TEST_ASSERT_NULL(security_get_csr());
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_get_private_key(&key_id));

    // Revoking again, when nothing is left, is not an error
    TEST_ASSERT_EQUAL(ESP_OK, security_revoke());
}
