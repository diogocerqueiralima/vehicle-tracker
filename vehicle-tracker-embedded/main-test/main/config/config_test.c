#include <stdlib.h>
#include <string.h>

#include "config.h"
#include "../utils/crypto_fixtures.h"
#include "esp_err.h"
#include "nvs.h"
#include "psa/crypto.h"
#include "security.h"
#include "storage.h"
#include "unity.h"

#define TEST_TAG "[config]"

#define STATE_CTX ((storage_ctx_t){.namespace = "security", .key = "state", .mode = STORAGE_MODE_READ_WRITE})
#define CSR_CTX ((storage_ctx_t){.namespace = "security", .key = "csr", .mode = STORAGE_MODE_READ_WRITE})
#define CERT_CTX ((storage_ctx_t){.namespace = "security", .key = "cert", .mode = STORAGE_MODE_READ_WRITE})

// Storage contexts of the properties that are stored by the configuration layer
#define KEEP_ALIVE_CTX ((storage_ctx_t){.namespace = "connection", .key = "keep_alive", .mode = STORAGE_MODE_READ_WRITE})
#define GPS_MODE_CTX ((storage_ctx_t){.namespace = "gps", .key = "gps_mode", .mode = STORAGE_MODE_READ_WRITE})

// An ID that does not belong to any property
#define UNKNOWN_ID ((config_id_t) 999)

// --- Helpers ---

/**
 * Saves the lifecycle state and loads it into the security layer, like a boot of a device that was in that state.
 */
static void boot_security(const security_state_t state) {
    // A previous boot of the same test may have left the security layer initialized
    const esp_err_t destroy_err = security_destroy();
    TEST_ASSERT_TRUE(destroy_err == ESP_OK || destroy_err == ESP_ERR_INVALID_STATE);

    const storage_ctx_t ctx = STATE_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_u32(&ctx, state));
    TEST_ASSERT_EQUAL(ESP_OK, security_init());
}

/**
 * Reads an integer property and frees the value.
 */
static int32_t get_int(const config_id_t id) {
    void *value = nullptr;
    TEST_ASSERT_EQUAL(ESP_OK, config_get(id, &value));
    TEST_ASSERT_NOT_NULL(value);

    const int32_t result = *(int32_t *) value;
    free(value);
    return result;
}

/**
 * Reads a string property, the caller must free it.
 */
static char *get_string(const config_id_t id) {
    void *value = nullptr;
    TEST_ASSERT_EQUAL(ESP_OK, config_get(id, &value));
    TEST_ASSERT_NOT_NULL(value);
    return value;
}

// --- Initialization ---

TEST_CASE("init writes the default of every property that has one", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    TEST_ASSERT_EQUAL_INT32(60, get_int(CONFIG_KEEP_ALIVE));
    TEST_ASSERT_EQUAL_INT32(0, get_int(CONFIG_QOS));
    TEST_ASSERT_EQUAL_INT32(30, get_int(CONFIG_RECONNECT_INTERVAL));
    TEST_ASSERT_EQUAL_INT32(60, get_int(CONFIG_GPS_UPDATE_INTERVAL));
    TEST_ASSERT_EQUAL_INT32(60, get_int(CONFIG_GPS_TIMEOUT));

    char *mode = get_string(CONFIG_GPS_MODE);
    TEST_ASSERT_EQUAL_STRING("ue-based", mode);
    free(mode);
}

TEST_CASE("init leaves the properties without a default unset", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    void *value = nullptr;
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, config_get(CONFIG_BROKER_URL, &value));
    TEST_ASSERT_NULL(value);
}

TEST_CASE("init does not overwrite a value that is already set", TEST_TAG) {
    const storage_ctx_t keep_alive_ctx = KEEP_ALIVE_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_i32(&keep_alive_ctx, 15));
    const storage_ctx_t mode_ctx = GPS_MODE_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&mode_ctx, "standalone"));

    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    TEST_ASSERT_EQUAL_INT32(15, get_int(CONFIG_KEEP_ALIVE));
    char *mode = get_string(CONFIG_GPS_MODE);
    TEST_ASSERT_EQUAL_STRING("standalone", mode);
    free(mode);

    // The others still get their default
    TEST_ASSERT_EQUAL_INT32(30, get_int(CONFIG_RECONNECT_INTERVAL));
}

TEST_CASE("init does not replace a value changed after the first init", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    // A zero is a real value, not a missing one
    const int32_t qos = 2;
    TEST_ASSERT_EQUAL(ESP_OK, config_set(CONFIG_QOS, &qos));

    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    TEST_ASSERT_EQUAL_INT32(2, get_int(CONFIG_QOS));
}

// --- Reading ---

TEST_CASE("get fails when the value was never set", TEST_TAG) {
    void *value = nullptr;
    TEST_ASSERT_EQUAL(ESP_ERR_NVS_NOT_FOUND, config_get(CONFIG_KEEP_ALIVE, &value));
    TEST_ASSERT_NULL(value);
}

TEST_CASE("get rejects a null output", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_get(CONFIG_KEEP_ALIVE, nullptr));
}

TEST_CASE("get rejects an unknown id", TEST_TAG) {
    void *value = nullptr;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_get(UNKNOWN_ID, &value));
    TEST_ASSERT_NULL(value);
}

TEST_CASE("get rejects a property that is not readable", TEST_TAG) {
    void *value = nullptr;
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_SUPPORTED, config_get(CONFIG_REVOKE, &value));
    TEST_ASSERT_NULL(value);
}

TEST_CASE("get returns the status of the security layer as text", TEST_TAG) {
    const struct {
        security_state_t state;
        const char *name;
    } cases[] = {
        {SECURITY_STATE_UNPROVISIONED, "UNPROVISIONED"}, {SECURITY_STATE_ENROLLING, "ENROLLING"},
        {SECURITY_STATE_ACTIVE, "ACTIVE"},               {SECURITY_STATE_RENEWING, "RENEWING"},
        {SECURITY_STATE_EXPIRED, "EXPIRED"},             {SECURITY_STATE_REVOKED, "REVOKED"},
    };

    for (size_t i = 0; i < sizeof(cases) / sizeof(cases[0]); i++) {
        boot_security(cases[i].state);

        char *status = get_string(CONFIG_STATUS);
        TEST_ASSERT_EQUAL_STRING(cases[i].name, status);
        free(status);
    }
}

TEST_CASE("get returns the certificate of the security layer", TEST_TAG) {
    boot_security(SECURITY_STATE_ACTIVE);
    const storage_ctx_t cert_ctx = CERT_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, CERT_A_VALID));

    char *certificate = get_string(CONFIG_CERTIFICATE);
    TEST_ASSERT_EQUAL_STRING(CERT_A_VALID, certificate);
    free(certificate);
}

TEST_CASE("get returns the certificate authority of the security layer", TEST_TAG) {
    boot_security(SECURITY_STATE_UNPROVISIONED);
    TEST_ASSERT_EQUAL(ESP_OK, security_install_certificate_authority(CERT_CA_VALID));

    char *authority = get_string(CONFIG_CERTIFICATE_AUTHORITY);
    TEST_ASSERT_EQUAL_STRING(CERT_CA_VALID, authority);
    free(authority);
}

TEST_CASE("get returns the csr of the security layer", TEST_TAG) {
    boot_security(SECURITY_STATE_ENROLLING);
    const storage_ctx_t csr_ctx = CSR_CTX;
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&csr_ctx, "csr"));

    char *csr = get_string(CONFIG_CSR);
    TEST_ASSERT_EQUAL_STRING("csr", csr);
    free(csr);
}

TEST_CASE("get csr does not start the enrollment", TEST_TAG) {
    const security_state_t states[] = {SECURITY_STATE_UNPROVISIONED, SECURITY_STATE_EXPIRED, SECURITY_STATE_REVOKED};
    const char *config_state_names[] = {"UNPROVISIONED", "EXPIRED", "REVOKED"};

    for (size_t i = 0; i < sizeof(states) / sizeof(states[0]); i++) {
        boot_security(states[i]);

        void *value = nullptr;
        TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, config_get(CONFIG_CSR, &value));
        TEST_ASSERT_NULL(value);

        char *status = get_string(CONFIG_STATUS);
        TEST_ASSERT_EQUAL_STRING(config_state_names[i], status);
        free(status);
    }
}

TEST_CASE("set enroll false does nothing", TEST_TAG) {
    boot_security(SECURITY_STATE_UNPROVISIONED);

    const bool enroll = false;
    TEST_ASSERT_EQUAL(ESP_OK, config_set(CONFIG_ENROLL, &enroll));

    char *status = get_string(CONFIG_STATUS);
    TEST_ASSERT_EQUAL_STRING("UNPROVISIONED", status);
    free(status);
}

TEST_CASE("set enroll is passed the error of the security layer", TEST_TAG) {
    boot_security(SECURITY_STATE_ACTIVE);

    const bool enroll = true;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, config_set(CONFIG_ENROLL, &enroll));
}

TEST_CASE("get rejects the enroll command because it is not readable", TEST_TAG) {
    void *value = nullptr;
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_SUPPORTED, config_get(CONFIG_ENROLL, &value));
    TEST_ASSERT_NULL(value);
}

TEST_CASE("set enroll starts the enrollment and the csr becomes readable", TEST_TAG) {
    TEST_ASSERT_EQUAL(PSA_SUCCESS, psa_crypto_init());
    boot_security(SECURITY_STATE_UNPROVISIONED);

    const bool enroll = true;
    TEST_ASSERT_EQUAL(ESP_OK, config_set(CONFIG_ENROLL, &enroll));

    char *csr = get_string(CONFIG_CSR);
    TEST_ASSERT_EQUAL_STRING_LEN("-----BEGIN CERTIFICATE REQUEST-----", csr, 35);
    free(csr);

    char *status = get_string(CONFIG_STATUS);
    TEST_ASSERT_EQUAL_STRING("ENROLLING", status);
    free(status);

    const psa_status_t destroy_status = psa_destroy_key(PSA_KEY_ID_USER_MIN);
    TEST_ASSERT_TRUE(destroy_status == PSA_SUCCESS || destroy_status == PSA_ERROR_INVALID_HANDLE);
}

TEST_CASE("get passes the error of the security layer on", TEST_TAG) {
    // The certificate is only available in the ACTIVE and RENEWING states, and the csr is refused in them
    boot_security(SECURITY_STATE_UNPROVISIONED);

    void *value = nullptr;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, config_get(CONFIG_CERTIFICATE, &value));

    boot_security(SECURITY_STATE_ACTIVE);
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, config_get(CONFIG_CSR, &value));
    TEST_ASSERT_NULL(value);
}

TEST_CASE("get of an authentication value is rejected when the security layer is not initialized", TEST_TAG) {
    void *value = nullptr;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, config_get(CONFIG_STATUS, &value));
    TEST_ASSERT_NULL(value);
}

// --- Writing ---

TEST_CASE("set stores an integer that get returns", TEST_TAG) {
    const int32_t keep_alive = 120;
    TEST_ASSERT_EQUAL(ESP_OK, config_set(CONFIG_KEEP_ALIVE, &keep_alive));

    TEST_ASSERT_EQUAL_INT32(120, get_int(CONFIG_KEEP_ALIVE));

    // It is in storage, so it survives a reboot
    const storage_ctx_t ctx = KEEP_ALIVE_CTX;
    int32_t stored = 0;
    TEST_ASSERT_EQUAL(ESP_OK, storage_get_i32(&ctx, &stored));
    TEST_ASSERT_EQUAL_INT32(120, stored);
}

TEST_CASE("set stores a string that get returns", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_OK, config_set(CONFIG_BROKER_URL, "mqtts://broker.example.com:8883"));

    char *url = get_string(CONFIG_BROKER_URL);
    TEST_ASSERT_EQUAL_STRING("mqtts://broker.example.com:8883", url);
    free(url);
}

TEST_CASE("set replaces the previous value", TEST_TAG) {
    int32_t timeout = 30;
    TEST_ASSERT_EQUAL(ESP_OK, config_set(CONFIG_GPS_TIMEOUT, &timeout));
    timeout = 90;
    TEST_ASSERT_EQUAL(ESP_OK, config_set(CONFIG_GPS_TIMEOUT, &timeout));

    TEST_ASSERT_EQUAL_INT32(90, get_int(CONFIG_GPS_TIMEOUT));
}

TEST_CASE("set rejects a null value", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set(CONFIG_KEEP_ALIVE, nullptr));
}

TEST_CASE("set rejects an unknown id", TEST_TAG) {
    const int32_t value = 1;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set(UNKNOWN_ID, &value));
}

TEST_CASE("set rejects the properties that are not writable", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_SUPPORTED, config_set(CONFIG_CSR, "csr"));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_SUPPORTED, config_set(CONFIG_STATUS, "ACTIVE"));
}

TEST_CASE("set installs the certificate authority in the security layer", TEST_TAG) {
    boot_security(SECURITY_STATE_UNPROVISIONED);

    TEST_ASSERT_EQUAL(ESP_OK, config_set(CONFIG_CERTIFICATE_AUTHORITY, CERT_CA_VALID));

    char *authority = nullptr;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_certificate_authority(&authority));
    TEST_ASSERT_EQUAL_STRING(CERT_CA_VALID, authority);
    free(authority);
}

TEST_CASE("set passes the error of the security layer on", TEST_TAG) {
    boot_security(SECURITY_STATE_UNPROVISIONED);

    // Not a certificate authority, and the device is not waiting for a certificate
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set(CONFIG_CERTIFICATE_AUTHORITY, "not a certificate"));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, config_set(CONFIG_CERTIFICATE, CERT_A_VALID));
}

TEST_CASE("set of an authentication value is rejected when the security layer is not initialized", TEST_TAG) {
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, config_set(CONFIG_CERTIFICATE_AUTHORITY, CERT_CA_VALID));
}

TEST_CASE("set revoke with true asks the security layer to revoke", TEST_TAG) {
    // An unprovisioned device has nothing to revoke, so the security layer refuses it
    boot_security(SECURITY_STATE_UNPROVISIONED);

    const bool revoke = true;
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, config_set(CONFIG_REVOKE, &revoke));
}

TEST_CASE("set revoke with false does nothing", TEST_TAG) {
    boot_security(SECURITY_STATE_UNPROVISIONED);

    const bool revoke = false;
    TEST_ASSERT_EQUAL(ESP_OK, config_set(CONFIG_REVOKE, &revoke));

    security_context_t context;
    TEST_ASSERT_EQUAL(ESP_OK, security_get_context(&context));
    TEST_ASSERT_EQUAL(SECURITY_STATE_UNPROVISIONED, context.state);
}
