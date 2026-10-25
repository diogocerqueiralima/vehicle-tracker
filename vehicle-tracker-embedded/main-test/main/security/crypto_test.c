#include <string.h>

#include "internal/crypto.h"

#include "crypto_fixtures.h"
#include "esp_err.h"
#include "mbedtls/x509_csr.h"
#include "unity.h"

#define TEST_TAG "[crypto]"

// Id used by the tests, so they never touch a real key
#define TEST_KEY_ID (PSA_KEY_ID_USER_MIN + 100)

// Common name used by the CSR tests
#define TEST_COMMON_NAME "123e4567-e89b-42d3-a456-426614174000"

// Size of a buffer able to hold any CSR generated with the test common name
#define CSR_BUFFER_SIZE 512

// Size of the raw public key of an ECDSA P-256 key, an uncompressed point
#define RAW_PUBLIC_KEY_SIZE 65

// --- Helpers ---

/**
 * Removes the test key, so every test starts and ends without one.
 */
static void reset_key() {
    TEST_ASSERT_EQUAL(ESP_OK, crypto_destroy_private_key(TEST_KEY_ID));
}

/**
 * Imports a raw ECDSA P-256 private key under the test id, with the same policy the crypto functions use.
 */
static void import_key(const uint8_t *raw, const size_t size) {
    TEST_ASSERT_EQUAL(PSA_SUCCESS, psa_crypto_init());

    psa_key_attributes_t attributes = PSA_KEY_ATTRIBUTES_INIT;
    psa_set_key_id(&attributes, TEST_KEY_ID);
    psa_set_key_lifetime(&attributes, PSA_KEY_LIFETIME_PERSISTENT);
    psa_set_key_type(&attributes, PSA_KEY_TYPE_ECC_KEY_PAIR(PSA_ECC_FAMILY_SECP_R1));
    psa_set_key_bits(&attributes, 256);
    psa_set_key_usage_flags(&attributes, PSA_KEY_USAGE_SIGN_HASH | PSA_KEY_USAGE_SIGN_MESSAGE);
    psa_set_key_algorithm(&attributes, PSA_ALG_ECDSA(PSA_ALG_SHA_256));

    psa_key_id_t id = PSA_KEY_ID_NULL;
    TEST_ASSERT_EQUAL(PSA_SUCCESS, psa_import_key(&attributes, raw, size, &id));
    psa_reset_key_attributes(&attributes);
}

/**
 * Exports the raw public key of the test key.
 */
static void export_public_key(uint8_t out[RAW_PUBLIC_KEY_SIZE]) {
    size_t length = 0;
    TEST_ASSERT_EQUAL(PSA_SUCCESS, psa_export_public_key(TEST_KEY_ID, out, RAW_PUBLIC_KEY_SIZE, &length));
    TEST_ASSERT_EQUAL(RAW_PUBLIC_KEY_SIZE, length);
}

// --- Private key ---

TEST_CASE("generate private key creates a persistent P-256 key pair", TEST_TAG) {
    reset_key();

    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_private_key(TEST_KEY_ID));

    psa_key_attributes_t attributes = PSA_KEY_ATTRIBUTES_INIT;
    TEST_ASSERT_EQUAL(PSA_SUCCESS, psa_get_key_attributes(TEST_KEY_ID, &attributes));
    TEST_ASSERT_EQUAL(PSA_KEY_TYPE_ECC_KEY_PAIR(PSA_ECC_FAMILY_SECP_R1), psa_get_key_type(&attributes));
    TEST_ASSERT_EQUAL(256, psa_get_key_bits(&attributes));
    TEST_ASSERT_EQUAL(PSA_KEY_LIFETIME_PERSISTENT, psa_get_key_lifetime(&attributes));
    TEST_ASSERT_EQUAL(PSA_ALG_ECDSA(PSA_ALG_SHA_256), psa_get_key_algorithm(&attributes));
    psa_reset_key_attributes(&attributes);

    reset_key();
}

TEST_CASE("generated private key cannot be exported", TEST_TAG) {
    reset_key();
    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_private_key(TEST_KEY_ID));

    uint8_t buffer[RAW_PUBLIC_KEY_SIZE];
    size_t length = 0;
    TEST_ASSERT_EQUAL(PSA_ERROR_NOT_PERMITTED, psa_export_key(TEST_KEY_ID, buffer, sizeof(buffer), &length));

    reset_key();
}

TEST_CASE("generate private key overrides an existing key", TEST_TAG) {
    reset_key();

    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_private_key(TEST_KEY_ID));
    uint8_t first[RAW_PUBLIC_KEY_SIZE];
    export_public_key(first);

    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_private_key(TEST_KEY_ID));
    uint8_t second[RAW_PUBLIC_KEY_SIZE];
    export_public_key(second);

    TEST_ASSERT_NOT_EQUAL(0, memcmp(first, second, RAW_PUBLIC_KEY_SIZE));

    reset_key();
}

TEST_CASE("destroy private key removes the key", TEST_TAG) {
    reset_key();
    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_private_key(TEST_KEY_ID));

    TEST_ASSERT_EQUAL(ESP_OK, crypto_destroy_private_key(TEST_KEY_ID));

    psa_key_attributes_t attributes = PSA_KEY_ATTRIBUTES_INIT;
    TEST_ASSERT_EQUAL(PSA_ERROR_INVALID_HANDLE, psa_get_key_attributes(TEST_KEY_ID, &attributes));
}

TEST_CASE("destroy private key succeeds when there is no key", TEST_TAG) {
    reset_key();

    TEST_ASSERT_EQUAL(ESP_OK, crypto_destroy_private_key(TEST_KEY_ID));
}

// --- CSR ---

TEST_CASE("generate csr rejects null arguments", TEST_TAG) {
    char pem[CSR_BUFFER_SIZE];

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_generate_csr(TEST_KEY_ID, nullptr, pem, sizeof(pem)));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_generate_csr(TEST_KEY_ID, TEST_COMMON_NAME, nullptr, sizeof(pem)));
}

TEST_CASE("generate csr rejects a common name that is too long", TEST_TAG) {
    char common_name[256];
    memset(common_name, 'a', sizeof(common_name) - 1);
    common_name[sizeof(common_name) - 1] = '\0';

    char pem[CSR_BUFFER_SIZE];
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_generate_csr(TEST_KEY_ID, common_name, pem, sizeof(pem)));
}

TEST_CASE("generate csr fails when the key does not exist", TEST_TAG) {
    reset_key();

    char pem[CSR_BUFFER_SIZE];
    TEST_ASSERT_EQUAL(ESP_FAIL, crypto_generate_csr(TEST_KEY_ID, TEST_COMMON_NAME, pem, sizeof(pem)));
}

TEST_CASE("generate csr fails when the buffer is too small", TEST_TAG) {
    reset_key();
    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_private_key(TEST_KEY_ID));

    char pem[64];
    TEST_ASSERT_EQUAL(ESP_FAIL, crypto_generate_csr(TEST_KEY_ID, TEST_COMMON_NAME, pem, sizeof(pem)));

    reset_key();
}

TEST_CASE("generated csr is a null terminated PEM that fits the expected size", TEST_TAG) {
    reset_key();
    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_private_key(TEST_KEY_ID));

    char pem[CSR_BUFFER_SIZE];
    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_csr(TEST_KEY_ID, TEST_COMMON_NAME, pem, sizeof(pem)));

    const size_t length = strlen(pem);
    TEST_ASSERT_LESS_THAN(sizeof(pem), length);
    TEST_ASSERT_EQUAL_STRING_LEN("-----BEGIN CERTIFICATE REQUEST-----", pem, 35);
    TEST_ASSERT_NOT_NULL(strstr(pem, "-----END CERTIFICATE REQUEST-----"));

    reset_key();
}

TEST_CASE("generated csr has a valid signature, the common name and the public key of the private key", TEST_TAG) {
    reset_key();
    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_private_key(TEST_KEY_ID));

    char pem[CSR_BUFFER_SIZE];
    TEST_ASSERT_EQUAL(ESP_OK, crypto_generate_csr(TEST_KEY_ID, TEST_COMMON_NAME, pem, sizeof(pem)));

    // Parsing a CSR also verifies its signature
    mbedtls_x509_csr csr;
    mbedtls_x509_csr_init(&csr);
    TEST_ASSERT_EQUAL(0, mbedtls_x509_csr_parse(&csr, (const unsigned char *) pem, strlen(pem) + 1));

    char subject[128];
    TEST_ASSERT_GREATER_THAN(0, mbedtls_x509_dn_gets(subject, sizeof(subject), &csr.subject));
    TEST_ASSERT_EQUAL_STRING("CN=" TEST_COMMON_NAME, subject);

    // The raw public key is the tail of the SubjectPublicKeyInfo DER
    unsigned char der[128];
    const int der_length = mbedtls_pk_write_pubkey_der(&csr.pk, der, sizeof(der));
    TEST_ASSERT_GREATER_OR_EQUAL(RAW_PUBLIC_KEY_SIZE, der_length);

    uint8_t expected[RAW_PUBLIC_KEY_SIZE];
    export_public_key(expected);
    TEST_ASSERT_EQUAL_MEMORY(expected, der + sizeof(der) - RAW_PUBLIC_KEY_SIZE, RAW_PUBLIC_KEY_SIZE);

    mbedtls_x509_csr_free(&csr);
    reset_key();
}

// --- Certificate parsing ---

TEST_CASE("parse certificate rejects null arguments", TEST_TAG) {
    mbedtls_x509_crt crt;

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_parse_certificate(nullptr, &crt));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_parse_certificate(CERT_A_VALID, nullptr));
}

TEST_CASE("parse certificate accepts a PEM certificate", TEST_TAG) {
    mbedtls_x509_crt crt;

    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_A_VALID, &crt));

    char subject[128];
    TEST_ASSERT_GREATER_THAN(0, mbedtls_x509_dn_gets(subject, sizeof(subject), &crt.subject));
    TEST_ASSERT_EQUAL_STRING("CN=test-device", subject);

    mbedtls_x509_crt_free(&crt);
}

TEST_CASE("parse certificate rejects text that is not a certificate", TEST_TAG) {
    mbedtls_x509_crt crt;

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_parse_certificate("", &crt));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_parse_certificate("not a certificate", &crt));
}

TEST_CASE("parse certificate rejects a corrupted certificate", TEST_TAG) {
    char corrupted[sizeof(CERT_A_VALID)];
    memcpy(corrupted, CERT_A_VALID, sizeof(CERT_A_VALID));

    // Change a character in the middle of the base64 body
    corrupted[100] = corrupted[100] == 'A' ? 'B' : 'A';

    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_parse_certificate(corrupted, &crt));
}

// --- Certificate and key ---

TEST_CASE("certificate matches key rejects null arguments", TEST_TAG) {
    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_A_VALID, &crt));

    bool matches = false;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_certificate_matches_key(nullptr, TEST_KEY_ID, &matches));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_certificate_matches_key(&crt, TEST_KEY_ID, nullptr));

    mbedtls_x509_crt_free(&crt);
}

TEST_CASE("certificate matches the key it was issued for", TEST_TAG) {
    reset_key();
    import_key(KEY_A_RAW, sizeof(KEY_A_RAW));

    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_A_VALID, &crt));

    bool matches = false;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_certificate_matches_key(&crt, TEST_KEY_ID, &matches));
    TEST_ASSERT_TRUE(matches);

    mbedtls_x509_crt_free(&crt);
    reset_key();
}

TEST_CASE("certificate does not match a different key", TEST_TAG) {
    reset_key();
    import_key(KEY_B_RAW, sizeof(KEY_B_RAW));

    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_A_VALID, &crt));

    bool matches = true;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_certificate_matches_key(&crt, TEST_KEY_ID, &matches));
    TEST_ASSERT_FALSE(matches);

    mbedtls_x509_crt_free(&crt);
    reset_key();
}

TEST_CASE("certificate with a bigger public key than the device key does not match and is not an error", TEST_TAG) {
    reset_key();
    import_key(KEY_A_RAW, sizeof(KEY_A_RAW));

    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_RSA_VALID, &crt));

    bool matches = true;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_certificate_matches_key(&crt, TEST_KEY_ID, &matches));
    TEST_ASSERT_FALSE(matches);

    mbedtls_x509_crt_free(&crt);
    reset_key();
}

TEST_CASE("certificate matches key fails when the key does not exist", TEST_TAG) {
    reset_key();

    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_A_VALID, &crt));

    bool matches = true;
    TEST_ASSERT_EQUAL(ESP_FAIL, crypto_certificate_matches_key(&crt, TEST_KEY_ID, &matches));

    mbedtls_x509_crt_free(&crt);
}

// --- Certificate validity ---

TEST_CASE("certificate is valid rejects null arguments", TEST_TAG) {
    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_A_VALID, &crt));

    bool valid = false;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_certificate_is_valid(nullptr, &valid));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, crypto_certificate_is_valid(&crt, nullptr));

    mbedtls_x509_crt_free(&crt);
}

TEST_CASE("certificate inside its validity period is valid", TEST_TAG) {
    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_A_VALID, &crt));

    bool valid = false;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_certificate_is_valid(&crt, &valid));
    TEST_ASSERT_TRUE(valid);

    mbedtls_x509_crt_free(&crt);
}

TEST_CASE("expired certificate is not valid", TEST_TAG) {
    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_A_EXPIRED, &crt));

    bool valid = true;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_certificate_is_valid(&crt, &valid));
    TEST_ASSERT_FALSE(valid);

    mbedtls_x509_crt_free(&crt);
}

TEST_CASE("certificate that is not yet valid is not valid", TEST_TAG) {
    mbedtls_x509_crt crt;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_parse_certificate(CERT_A_FUTURE, &crt));

    bool valid = true;
    TEST_ASSERT_EQUAL(ESP_OK, crypto_certificate_is_valid(&crt, &valid));
    TEST_ASSERT_FALSE(valid);

    mbedtls_x509_crt_free(&crt);
}
