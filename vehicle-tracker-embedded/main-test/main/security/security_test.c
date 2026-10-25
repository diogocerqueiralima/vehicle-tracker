#include <stdlib.h>
#include <string.h>

#include "crypto_fixtures.h"
#include "esp_err.h"
#include "mbedtls/x509_csr.h"
#include "psa/crypto.h"
#include "security.h"

#include "nvs.h"
#include "storage.h"
#include "unity.h"
#include "uuid.h"

#define TEST_TAG "[security]"

// Storage contexts used by the security layer, they must stay in sync with security.c
#define STATE_CTX ((storage_ctx_t){.namespace = "security", .key = "state", .mode = STORAGE_MODE_READ_WRITE})
#define CSR_CTX ((storage_ctx_t){.namespace = "security", .key = "csr", .mode = STORAGE_MODE_READ_WRITE})
#define CERT_CTX ((storage_ctx_t){.namespace = "security", .key = "cert", .mode = STORAGE_MODE_READ_WRITE})

// Id of the device private key, it must stay in sync with PRIVATE_KEY_ID in security.c
#define DEVICE_KEY_ID PSA_KEY_ID_USER_MIN

// --- Helpers ---

/**
 * Removes the device private key, so every test starts and ends without one.
 */
static void reset_key() {
    TEST_ASSERT_EQUAL(PSA_SUCCESS, psa_crypto_init());

    const psa_status_t status = psa_destroy_key(DEVICE_KEY_ID);
    TEST_ASSERT_TRUE(status == PSA_SUCCESS || status == PSA_ERROR_INVALID_HANDLE);
}

/**
 * Imports a raw ECDSA P-256 private key as the device private key, with the same policy the crypto functions use.
 */
static void import_device_key(const uint8_t *raw, const size_t size) {
    reset_key();

    psa_key_attributes_t attributes = PSA_KEY_ATTRIBUTES_INIT;
    psa_set_key_id(&attributes, DEVICE_KEY_ID);
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
 * Tells if the device private key exists.
 */
static bool device_key_exists() {
    psa_key_attributes_t attributes = PSA_KEY_ATTRIBUTES_INIT;
    const psa_status_t status = psa_get_key_attributes(DEVICE_KEY_ID, &attributes);
    psa_reset_key_attributes(&attributes);
    return status == PSA_SUCCESS;
}

/**
 * Saves the lifecycle state and loads it into the security context, like a boot of a device that was in that state.
 */
static void boot_in_state(const security_state_t state) {
    const storage_ctx_t ctx = STATE_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, state));
    TEST_ASSERT_EQUAL(ESP_OK, security_init());
}

/**
 * Gets the lifecycle state from the security context.
 */
static security_state_t context_state() {
    security_context_t context;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_context(&context));
    return context.state;
}

/**
 * Gets the lifecycle state saved in flash.
 */
static security_state_t stored_state() {
    const storage_ctx_t ctx = STATE_CTX;
    uint32_t state = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_u32(&ctx, &state));
    return (security_state_t) state;
}

/**
 * Tells if there is a string saved under the context.
 */
static bool has_string(const storage_ctx_t *ctx) {
    size_t size = 0;
    const esp_err_t err = storage_get_str_size(ctx, &size);
    TEST_ASSERT_TRUE(err == ESP_OK || err == ESP_ERR_NVS_NOT_FOUND);
    return err == ESP_OK;
}

// --- Initialization ---

TEST_CASE("first boot starts as unprovisioned with a version 4 identity", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, security_init());

    security_context_t context;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_context(&context));
    TEST_ASSERT_EQUAL(SECURITY_STATE_UNPROVISIONED, context.state);
    TEST_ASSERT_EQUAL_HEX8(0x40, context.identity.bytes[6] & 0xF0);
    TEST_ASSERT_EQUAL(SECURITY_STATE_UNPROVISIONED, stored_state());
}

TEST_CASE("identity stays the same across boots", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, security_init());
    security_context_t first;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_context(&first));

    TEST_ASSERT_EQUAL(ESP_OK, security_init());
    security_context_t second;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_context(&second));

    TEST_ASSERT_EQUAL_MEMORY(first.identity.bytes, second.identity.bytes, UUID_BYTES_SIZE);
}

TEST_CASE("saved lifecycle state is loaded on boot", TEST_TAG) {
    boot_in_state(SECURITY_STATE_ACTIVE);

    TEST_ASSERT_EQUAL(SECURITY_STATE_ACTIVE, context_state());
}

TEST_CASE("init rejects a saved state that is not a known one and stays not initialized", TEST_TAG) {
    const storage_ctx_t ctx = STATE_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, SECURITY_STATE_REVOKED + 1));

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_init());

    security_context_t context;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_get_context(&context));
}

// --- Enrollment ---

TEST_CASE("enrollment from unprovisioned creates the key and the csr and transitions to enrolling", TEST_TAG) {
    reset_key();
    boot_in_state(SECURITY_STATE_UNPROVISIONED);

    TEST_ASSERT_EQUAL(ESP_OK, security_start_enrollment());

    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, stored_state());
    TEST_ASSERT_TRUE(device_key_exists());

    const storage_ctx_t csr_ctx = CSR_CTX;
    char csr[512];
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_str(&csr_ctx, csr, sizeof(csr)));
    TEST_ASSERT_EQUAL_STRING_LEN("-----BEGIN CERTIFICATE REQUEST-----", csr, 35);

    reset_key();
}

TEST_CASE("csr uses the device identity as common name", TEST_TAG) {
    reset_key();
    boot_in_state(SECURITY_STATE_UNPROVISIONED);
    TEST_ASSERT_EQUAL(ESP_OK, security_start_enrollment());

    security_context_t context;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_context(&context));
    char identity[UUID_STR_SIZE];
    TEST_ASSERT_EQUAL(ESP_OK, uuid_to_str(&context.identity, identity, sizeof(identity)));

    // The common name is in the DER of the CSR, which is base64 encoded in the PEM, so parse it back
    const storage_ctx_t csr_ctx = CSR_CTX;
    char csr[512];
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_str(&csr_ctx, csr, sizeof(csr)));

    mbedtls_x509_csr parsed;
    mbedtls_x509_csr_init(&parsed);
    TEST_ASSERT_EQUAL(0, mbedtls_x509_csr_parse(&parsed, (const unsigned char *) csr, strlen(csr) + 1));

    char subject[128];
    TEST_ASSERT_GREATER_THAN(0, mbedtls_x509_dn_gets(subject, sizeof(subject), &parsed.subject));

    char expected[128];
    snprintf(expected, sizeof(expected), "CN=%s", identity);
    TEST_ASSERT_EQUAL_STRING(expected, subject);

    mbedtls_x509_csr_free(&parsed);
    reset_key();
}

TEST_CASE("enrollment is allowed again after expiring or being revoked", TEST_TAG) {
    const security_state_t states[] = {SECURITY_STATE_EXPIRED, SECURITY_STATE_REVOKED};

    for (size_t i = 0; i < sizeof(states) / sizeof(states[0]); i++) {
        reset_key();
        boot_in_state(states[i]);

        TEST_ASSERT_EQUAL(ESP_OK, security_start_enrollment());
        TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, context_state());
        TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, stored_state());
    }

    reset_key();
}

TEST_CASE("enrollment is rejected in the states that already have or wait for a certificate", TEST_TAG) {
    const security_state_t states[] = {SECURITY_STATE_ENROLLING, SECURITY_STATE_ACTIVE, SECURITY_STATE_RENEWING};

    for (size_t i = 0; i < sizeof(states) / sizeof(states[0]); i++) {
        reset_key();
        boot_in_state(states[i]);

        TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_start_enrollment());
        TEST_ASSERT_EQUAL(states[i], context_state());
        TEST_ASSERT_EQUAL(states[i], stored_state());
        TEST_ASSERT_FALSE(device_key_exists());
    }
}

// --- Certificate installation ---

TEST_CASE("install accepts a valid certificate of the device key and transitions to active", TEST_TAG) {
    import_device_key(KEY_A_RAW, sizeof(KEY_A_RAW));
    boot_in_state(SECURITY_STATE_ENROLLING);
    const storage_ctx_t saved_csr_ctx = CSR_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&saved_csr_ctx, "csr"));

    TEST_ASSERT_EQUAL(ESP_OK, security_install_certificate(CERT_A_VALID));

    TEST_ASSERT_EQUAL(SECURITY_STATE_ACTIVE, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_ACTIVE, stored_state());

    const storage_ctx_t cert_ctx = CERT_CTX;
    char saved[sizeof(CERT_A_VALID)];
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_str(&cert_ctx, saved, sizeof(saved)));
    TEST_ASSERT_EQUAL_STRING(CERT_A_VALID, saved);

    // The CSR is not needed anymore
    const storage_ctx_t csr_ctx = CSR_CTX;
    TEST_ASSERT_FALSE(has_string(&csr_ctx));

    reset_key();
}

TEST_CASE("install is rejected outside the enrolling state", TEST_TAG) {
    const security_state_t states[] = {SECURITY_STATE_UNPROVISIONED, SECURITY_STATE_ACTIVE, SECURITY_STATE_RENEWING,
                                       SECURITY_STATE_EXPIRED, SECURITY_STATE_REVOKED};

    import_device_key(KEY_A_RAW, sizeof(KEY_A_RAW));

    for (size_t i = 0; i < sizeof(states) / sizeof(states[0]); i++) {
        boot_in_state(states[i]);

        TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_install_certificate(CERT_A_VALID));
        TEST_ASSERT_EQUAL(states[i], context_state());

        const storage_ctx_t cert_ctx = CERT_CTX;
        TEST_ASSERT_FALSE(has_string(&cert_ctx));
    }

    reset_key();
}

TEST_CASE("install rejects text that is not a certificate and stays enrolling", TEST_TAG) {
    import_device_key(KEY_A_RAW, sizeof(KEY_A_RAW));
    boot_in_state(SECURITY_STATE_ENROLLING);

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_install_certificate("not a certificate"));

    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, stored_state());
    const storage_ctx_t cert_ctx = CERT_CTX;
    TEST_ASSERT_FALSE(has_string(&cert_ctx));

    reset_key();
}

TEST_CASE("install rejects a certificate of another key and stays enrolling", TEST_TAG) {
    import_device_key(KEY_B_RAW, sizeof(KEY_B_RAW));
    boot_in_state(SECURITY_STATE_ENROLLING);

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_install_certificate(CERT_A_VALID));

    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, stored_state());
    const storage_ctx_t cert_ctx = CERT_CTX;
    TEST_ASSERT_FALSE(has_string(&cert_ctx));

    reset_key();
}

TEST_CASE("install rejects an expired certificate and stays enrolling", TEST_TAG) {
    import_device_key(KEY_A_RAW, sizeof(KEY_A_RAW));
    boot_in_state(SECURITY_STATE_ENROLLING);

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_install_certificate(CERT_A_EXPIRED));

    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, stored_state());
    const storage_ctx_t cert_ctx = CERT_CTX;
    TEST_ASSERT_FALSE(has_string(&cert_ctx));

    reset_key();
}

TEST_CASE("install rejects a certificate that is not yet valid and stays enrolling", TEST_TAG) {
    import_device_key(KEY_A_RAW, sizeof(KEY_A_RAW));
    boot_in_state(SECURITY_STATE_ENROLLING);

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_install_certificate(CERT_A_FUTURE));

    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, stored_state());

    reset_key();
}

TEST_CASE("install fails when the device has no private key and stays enrolling", TEST_TAG) {
    reset_key();
    boot_in_state(SECURITY_STATE_ENROLLING);

    TEST_ASSERT_EQUAL(ESP_FAIL, security_install_certificate(CERT_A_VALID));

    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, stored_state());
}

TEST_CASE("install rejects a certificate that does not match the key generated by the enrollment", TEST_TAG) {
    reset_key();
    boot_in_state(SECURITY_STATE_UNPROVISIONED);
    TEST_ASSERT_EQUAL(ESP_OK, security_start_enrollment());

    // The key was generated by the device, so a certificate made for another key must not be accepted
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_install_certificate(CERT_A_VALID));
    TEST_ASSERT_EQUAL(SECURITY_STATE_ENROLLING, context_state());

    reset_key();
}

// --- Revocation ---

TEST_CASE("revoke deletes the key, the certificate and the csr and transitions to revoked", TEST_TAG) {
    const security_state_t states[] = {SECURITY_STATE_ACTIVE, SECURITY_STATE_RENEWING};

    for (size_t i = 0; i < sizeof(states) / sizeof(states[0]); i++) {
        import_device_key(KEY_A_RAW, sizeof(KEY_A_RAW));
        boot_in_state(states[i]);

        const storage_ctx_t cert_ctx = CERT_CTX;
        const storage_ctx_t csr_ctx = CSR_CTX;
        TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, CERT_A_VALID));
        TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&csr_ctx, "csr"));

        TEST_ASSERT_EQUAL(ESP_OK, security_revoke());

        TEST_ASSERT_EQUAL(SECURITY_STATE_REVOKED, context_state());
        TEST_ASSERT_EQUAL(SECURITY_STATE_REVOKED, stored_state());
        TEST_ASSERT_FALSE(device_key_exists());
        TEST_ASSERT_FALSE(has_string(&cert_ctx));
        TEST_ASSERT_FALSE(has_string(&csr_ctx));
    }
}

TEST_CASE("revoke works when the certificate and the csr are already gone", TEST_TAG) {
    import_device_key(KEY_A_RAW, sizeof(KEY_A_RAW));
    boot_in_state(SECURITY_STATE_ACTIVE);

    TEST_ASSERT_EQUAL(ESP_OK, security_revoke());

    TEST_ASSERT_EQUAL(SECURITY_STATE_REVOKED, context_state());
    TEST_ASSERT_FALSE(device_key_exists());
}

TEST_CASE("revoke is rejected in the states that have no certificate to revoke and deletes nothing", TEST_TAG) {
    const security_state_t states[] = {SECURITY_STATE_UNPROVISIONED, SECURITY_STATE_ENROLLING, SECURITY_STATE_EXPIRED,
                                       SECURITY_STATE_REVOKED};

    import_device_key(KEY_A_RAW, sizeof(KEY_A_RAW));

    for (size_t i = 0; i < sizeof(states) / sizeof(states[0]); i++) {
        boot_in_state(states[i]);

        TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_revoke());
        TEST_ASSERT_EQUAL(states[i], context_state());
        TEST_ASSERT_EQUAL(states[i], stored_state());
        TEST_ASSERT_TRUE(device_key_exists());
    }

    reset_key();
}

// --- Certificate retrieval ---

TEST_CASE("get certificate returns the installed certificate and keeps the active state", TEST_TAG) {
    boot_in_state(SECURITY_STATE_ACTIVE);
    const storage_ctx_t cert_ctx = CERT_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, CERT_A_VALID));

    char *pem = NULL;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_certificate(&pem));
    TEST_ASSERT_NOT_NULL(pem);
    TEST_ASSERT_EQUAL_STRING(CERT_A_VALID, pem);
    free(pem);

    TEST_ASSERT_EQUAL(SECURITY_STATE_ACTIVE, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_ACTIVE, stored_state());
}

TEST_CASE("get certificate returns an expired certificate and starts the renewal of an active device", TEST_TAG) {
    boot_in_state(SECURITY_STATE_ACTIVE);
    const storage_ctx_t cert_ctx = CERT_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, CERT_A_EXPIRED));

    char *pem = NULL;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_certificate(&pem));
    TEST_ASSERT_NOT_NULL(pem);
    TEST_ASSERT_EQUAL_STRING(CERT_A_EXPIRED, pem);
    free(pem);

    TEST_ASSERT_EQUAL(SECURITY_STATE_RENEWING, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_RENEWING, stored_state());
}

TEST_CASE("get certificate keeps a renewing device renewing", TEST_TAG) {
    boot_in_state(SECURITY_STATE_RENEWING);
    const storage_ctx_t cert_ctx = CERT_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, CERT_A_EXPIRED));

    char *pem = NULL;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_certificate(&pem));
    free(pem);

    TEST_ASSERT_EQUAL(SECURITY_STATE_RENEWING, context_state());
    TEST_ASSERT_EQUAL(SECURITY_STATE_RENEWING, stored_state());
}

TEST_CASE("get certificate is rejected in the states that have no certificate", TEST_TAG) {
    const security_state_t states[] = {SECURITY_STATE_UNPROVISIONED, SECURITY_STATE_ENROLLING, SECURITY_STATE_EXPIRED,
                                       SECURITY_STATE_REVOKED};

    for (size_t i = 0; i < sizeof(states) / sizeof(states[0]); i++) {
        boot_in_state(states[i]);

        char *pem = NULL;
        TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, security_get_certificate(&pem));
        TEST_ASSERT_NULL(pem);
    }
}

TEST_CASE("get certificate rejects a null output", TEST_TAG) {
    boot_in_state(SECURITY_STATE_ACTIVE);

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_get_certificate(NULL));
}

TEST_CASE("get certificate fails when no certificate is saved", TEST_TAG) {
    boot_in_state(SECURITY_STATE_ACTIVE);

    char *pem = NULL;
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, security_get_certificate(&pem));
    TEST_ASSERT_NULL(pem);
}

TEST_CASE("get certificate fails when the saved certificate is corrupted", TEST_TAG) {
    boot_in_state(SECURITY_STATE_ACTIVE);
    const storage_ctx_t cert_ctx = CERT_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, "not a certificate"));

    char *pem = NULL;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, security_get_certificate(&pem));
    TEST_ASSERT_NULL(pem);
    TEST_ASSERT_EQUAL(SECURITY_STATE_ACTIVE, context_state());
}
