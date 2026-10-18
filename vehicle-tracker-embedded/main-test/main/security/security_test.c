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

// The storage contexts of the values the security layer keeps, built from the keys it publishes
static const storage_ctx_t identity_ctx = STORAGE_RW(SECURITY_NAMESPACE, SECURITY_IDENTITY_KEY);
static const storage_ctx_t csr_ctx      = STORAGE_RW(SECURITY_NAMESPACE, SECURITY_CSR_KEY);
static const storage_ctx_t cert_ctx     = STORAGE_RW(SECURITY_NAMESPACE, SECURITY_CERT_KEY);
static const storage_ctx_t ca_ctx       = STORAGE_RW(SECURITY_NAMESPACE, SECURITY_CA_KEY);

#define PEM_HEADER "-----BEGIN CERTIFICATE REQUEST-----"
// Self-signed P-256 CA certificate, generated for these tests, also used as the device certificate
#define TEST_CA \
"-----BEGIN CERTIFICATE-----\n" \
"MIIBeTCCAR+gAwIBAgIUdard9L6FS6vwlUrep4YmccFPtFswCgYIKoZIzj0EAwIw\n" \
"EjEQMA4GA1UEAwwHVGVzdCBDQTAeFw0yNjEwMTQxODI2MTlaFw0zNjEwMTExODI2\n" \
"MTlaMBIxEDAOBgNVBAMMB1Rlc3QgQ0EwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNC\n" \
"AARxzGskExGuCIbwplxqovN9GjOPAz9hQ6XsLA/GSQDJdm+pb5PqxX6f2R1tOroe\n" \
"TFyqwyUZP7NFPUSZal+5NoiHo1MwUTAdBgNVHQ4EFgQUYnI8eeIpgmncNt8nBN39\n" \
"rwzNSC8wHwYDVR0jBBgwFoAUYnI8eeIpgmncNt8nBN39rwzNSC8wDwYDVR0TAQH/\n" \
"BAUwAwEB/zAKBggqhkjOPQQDAgNIADBFAiEAz04wJOFxH7fXSEp7E+vC7DOKH1s2\n" \
"Ebyik0xDTriPE8QCIHDhaqFbp3k6vUSu799qmRzXwfy/brP5Eq0UcLASLqfj\n" \
"-----END CERTIFICATE-----\n"
#define TEST_CERT TEST_CA
// Self-signed P-256 certificate whose validity ended on 2021-01-01, generated for these tests
#define TEST_EXPIRED_CERT \
"-----BEGIN CERTIFICATE-----\n" \
"MIIBgjCCASmgAwIBAgIUDNJlZpedaOqS40coIAUWz7BSuxgwCgYIKoZIzj0EAwIw\n" \
"FzEVMBMGA1UEAwwMRXhwaXJlZCBUZXN0MB4XDTIwMDEwMTAwMDAwMFoXDTIxMDEw\n" \
"MTAwMDAwMFowFzEVMBMGA1UEAwwMRXhwaXJlZCBUZXN0MFkwEwYHKoZIzj0CAQYI\n" \
"KoZIzj0DAQcDQgAEWJNJ0i77T1J1sjj72oHn2nVL/ifU1E75xGG1Wkp+kM+G5QHb\n" \
"x0BKTeYtJmGWv2aKG43/Edcls0npUOG67nFhO6NTMFEwHQYDVR0OBBYEFKOIRhcS\n" \
"moINUgGGCDHWeRg2a26kMB8GA1UdIwQYMBaAFKOIRhcSmoINUgGGCDHWeRg2a26k\n" \
"MA8GA1UdEwEB/wQFMAMBAf8wCgYIKoZIzj0EAwIDRwAwRAIgGaVsk+epmGhE3zqR\n" \
"H5W1ts8wKFoL51njD/z0YgSZKtYCIFHYPEGbgFP07yuBdOUamD72fPAr49r6swfy\n" \
"UrEPp3D7\n" \
"-----END CERTIFICATE-----\n"
#define SUBJECT_MAX_LEN 64
#define CSR_BUFFER_LEN  1024

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

    char uuid[UUID_STRING_LEN];
    TEST_ASSERT_EQUAL(ESP_OK, uuid_to_string(identity, uuid));
    char expected_subject[SUBJECT_MAX_LEN];
    snprintf(expected_subject, sizeof(expected_subject), "CN=%s", uuid);

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());

    char csr[CSR_BUFFER_LEN];
    TEST_ASSERT_EQUAL(ESP_OK, security_read_csr(csr, sizeof(csr)));
    TEST_ASSERT_NOT_NULL(strstr(csr, PEM_HEADER));
    assert_csr_subject(csr, expected_subject);
}

TEST_CASE("create csr fails when a CSR or a certificate is already stored", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_create_csr());

    TEST_ASSERT_EQUAL(ESP_OK, storage_erase(&csr_ctx));
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, TEST_CERT));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_create_csr());
}

// --- security_install_certificate ---

TEST_CASE("install certificate stores it, removes the stored request and keeps the key", TEST_TAG) {
    psa_key_id_t key_id = PSA_KEY_ID_NULL;
    size_t size = 0;

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_install_certificate(nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_install_certificate("not a certificate"));

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_OK, security_install_certificate(TEST_CERT));

    char certificate[sizeof(TEST_CERT)];
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_str(&cert_ctx, certificate, sizeof(certificate)));
    TEST_ASSERT_EQUAL_STRING(TEST_CERT, certificate);

    // The request is removed once the certificate is installed, but the key it was signed with stays
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_get_csr_size(&size));
    TEST_ASSERT_EQUAL(ESP_OK, security_get_private_key(&key_id));
}

TEST_CASE("install certificate rejects an invalid certificate and keeps the request", TEST_TAG) {
    size_t size = 0;

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_install_certificate("not a certificate"));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_install_certificate(""));

    // Nothing is stored, and the request is still there for the next attempt
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_str_size(&cert_ctx, &size));
    TEST_ASSERT_EQUAL_MESSAGE(ESP_OK, security_get_csr_size(&size), "the request must stay stored");
}

// --- security_set_ca_certificate ---

TEST_CASE("set ca certificate rejects invalid input without storing anything", TEST_TAG) {
    size_t size = 0;

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_set_ca_certificate(nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_set_ca_certificate("not a certificate"));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_set_ca_certificate(TEST_CA TEST_CA));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_str_size(&ca_ctx, &size));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_has_ca_certificate());
}

TEST_CASE("set ca certificate stores it and replaces the earlier one", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, security_set_ca_certificate(TEST_CA));
    TEST_ASSERT_EQUAL(ESP_OK, security_has_ca_certificate());

    TEST_ASSERT_EQUAL(ESP_OK, security_set_ca_certificate(TEST_CA));
    TEST_ASSERT_EQUAL(ESP_OK, security_has_ca_certificate());
}

TEST_CASE("has ca certificate is false until one is set", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_has_ca_certificate());

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_set_ca_certificate("not a certificate"));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_has_ca_certificate());

    TEST_ASSERT_EQUAL(ESP_OK, security_set_ca_certificate(TEST_CA));
    TEST_ASSERT_EQUAL(ESP_OK, security_has_ca_certificate());
}

TEST_CASE("set ca certificate rejects a PEM with more than one certificate", TEST_TAG) {
    size_t size = 0;

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_set_ca_certificate(TEST_CA TEST_CA));
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_str_size(&ca_ctx, &size));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_has_ca_certificate());
}

TEST_CASE("a rejected ca certificate keeps the installed one", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, security_set_ca_certificate(TEST_CA));

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_set_ca_certificate("not a certificate"));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_set_ca_certificate(nullptr));
    TEST_ASSERT_EQUAL(ESP_OK, security_has_ca_certificate());
}

TEST_CASE("ca certificates survive revoke", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, security_set_ca_certificate(TEST_CA));
    TEST_ASSERT_EQUAL(ESP_OK, security_revoke());
    TEST_ASSERT_EQUAL(ESP_OK, security_has_ca_certificate());
}

// --- security_init with stored CA certificates ---

TEST_CASE("init installs the stored ca certificates", TEST_TAG) {
    // Write the certificate straight to storage, so the global store is empty until init loads it
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&ca_ctx, TEST_CA));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_has_ca_certificate());

    TEST_ASSERT_EQUAL(ESP_OK, security_init());
    TEST_ASSERT_EQUAL(ESP_OK, security_has_ca_certificate());
}

TEST_CASE("has ca certificate reports an invalid state when the store is not the stored one", TEST_TAG) {
    // Installed, then removed from storage only
    TEST_ASSERT_EQUAL(ESP_OK, security_set_ca_certificate(TEST_CA));
    TEST_ASSERT_EQUAL(ESP_OK, storage_erase(&ca_ctx));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_has_ca_certificate());
}

TEST_CASE("has ca certificate reports an invalid state when more than one certificate is loaded", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&ca_ctx, TEST_CA TEST_CA));
    TEST_ASSERT_EQUAL(ESP_OK, security_init());
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_has_ca_certificate());
}

TEST_CASE("init fails and erases the stored ca certificate when it is not valid", TEST_TAG) {
    size_t size = 0;

    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&ca_ctx, "not a certificate\n"));
    TEST_ASSERT_EQUAL(ESP_FAIL, security_init());

    // The invalid certificate cannot be used, so it is gone from the storage layer
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, storage_get_str_size(&ca_ctx, &size));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_has_ca_certificate());

    // The next init, as after a restart, succeeds with an empty CA store
    TEST_ASSERT_EQUAL(ESP_OK, security_init());
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
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_get_csr_size(&size));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_get_private_key(&key_id));

    // Revoking again, when nothing is left, is not an error
    TEST_ASSERT_EQUAL(ESP_OK, security_revoke());
}

// --- error-aware PEM getters ---

TEST_CASE("pem getters report not found until the value is stored, then return it", TEST_TAG) {
    size_t size = 0;
    char buffer[2048];

    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_get_csr_size(&size));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_read_csr(buffer, sizeof(buffer)));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_get_certificate_size(&size));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_read_certificate(buffer, sizeof(buffer)));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_get_ca_certificate_size(&size));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_read_ca_certificate(buffer, sizeof(buffer)));

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_OK, security_get_csr_size(&size));
    TEST_ASSERT_EQUAL(ESP_OK, security_read_csr(buffer, size));
    TEST_ASSERT_EQUAL_UINT(size, strlen(buffer) + 1);

    TEST_ASSERT_EQUAL(ESP_OK, security_install_certificate(TEST_CERT));
    TEST_ASSERT_EQUAL(ESP_OK, security_get_certificate_size(&size));
    TEST_ASSERT_EQUAL_UINT(sizeof(TEST_CERT), size);
    TEST_ASSERT_EQUAL(ESP_OK, security_read_certificate(buffer, size));
    TEST_ASSERT_EQUAL_STRING(TEST_CERT, buffer);

    TEST_ASSERT_EQUAL(ESP_OK, security_set_ca_certificate(TEST_CA));
    TEST_ASSERT_EQUAL(ESP_OK, security_get_ca_certificate_size(&size));
    TEST_ASSERT_EQUAL(ESP_OK, security_read_ca_certificate(buffer, size));
    TEST_ASSERT_EQUAL_STRING(TEST_CA, buffer);
}

TEST_CASE("pem getters reject null arguments and a buffer that is too small", TEST_TAG) {
    char small[8];

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_get_csr_size(nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_read_certificate(nullptr, 10));

    TEST_ASSERT_EQUAL(ESP_OK, security_set_ca_certificate(TEST_CA));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_SIZE, security_read_ca_certificate(small, sizeof(small)));
}

// --- security_certificate_is_expired ---

TEST_CASE("certificate is expired reports not found without a certificate and not expired for a valid one", TEST_TAG) {
    bool expired = true;

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_certificate_is_expired(nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, security_certificate_is_expired(&expired));

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_OK, security_install_certificate(TEST_CERT));
    TEST_ASSERT_EQUAL(ESP_OK, security_certificate_is_expired(&expired));
    TEST_ASSERT_FALSE(expired);
}

TEST_CASE("certificate is expired reports an expired certificate", TEST_TAG) {
    bool expired = false;

    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_OK, security_install_certificate(TEST_EXPIRED_CERT));
    TEST_ASSERT_EQUAL(ESP_OK, security_certificate_is_expired(&expired));
    TEST_ASSERT_TRUE(expired);
}

TEST_CASE("certificate is expired rejects a stored certificate that cannot be parsed", TEST_TAG) {
    bool expired = false;

    // Stored behind the security layer's back, as a corrupt value would be
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, "not a certificate"));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_certificate_is_expired(&expired));
}
