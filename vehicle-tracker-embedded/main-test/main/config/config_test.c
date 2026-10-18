#include <stdlib.h>
#include <string.h>

#include "config.h"
#include "esp_err.h"
#include "security.h"
#include "storage.h"
#include "unity.h"

#define TEST_TAG "[config]"

// Self-signed P-256 certificate generated for the security tests, used as CA and as device certificate
#define TEST_CERT \
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

#define PEM_BUFFER_LEN 2048

/**
 * Reads a PEM value through a config getter into a fixed buffer, and checks the getter's contract: a string comes back
 * exactly when the error is ESP_OK. The caller's buffer receives a copy, so the allocated string is freed here.
 */
static esp_err_t copy_pem(char *(*getter)(esp_err_t *), char *buffer) {
    esp_err_t err = ESP_FAIL;
    char *pem = getter(&err);

    TEST_ASSERT_EQUAL_MESSAGE(err == ESP_OK, pem != nullptr, "a string must come back exactly when the read succeeds");

    if (pem != nullptr) {
        TEST_ASSERT_LESS_THAN(PEM_BUFFER_LEN, strlen(pem) + 1);
        strcpy(buffer, pem);
        free(pem);
    }

    return err;
}

#define read_csr(buffer)         copy_pem(config_get_csr, (buffer))
#define read_certificate(buffer) copy_pem(config_get_certificate, (buffer))
#define read_ca(buffer)          copy_pem(config_get_ca, (buffer))

// --- config_init ---

TEST_CASE("init writes the documented defaults", TEST_TAG) {
    uint32_t u32;
    uint8_t u8;
    config_gps_mode_t mode;

    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    TEST_ASSERT_EQUAL(ESP_OK, config_get_keep_alive(&u32));
    TEST_ASSERT_EQUAL_UINT32(60, u32);
    TEST_ASSERT_EQUAL(ESP_OK, config_get_qos(&u8));
    TEST_ASSERT_EQUAL_UINT8(0, u8);
    TEST_ASSERT_EQUAL(ESP_OK, config_get_recon_interval(&u32));
    TEST_ASSERT_EQUAL_UINT32(30, u32);
    TEST_ASSERT_EQUAL(ESP_OK, config_get_gps_update(&u32));
    TEST_ASSERT_EQUAL_UINT32(60, u32);
    TEST_ASSERT_EQUAL(ESP_OK, config_get_gps_timeout(&u32));
    TEST_ASSERT_EQUAL_UINT32(60, u32);
    TEST_ASSERT_EQUAL(ESP_OK, config_get_gps_mode(&mode));
    TEST_ASSERT_EQUAL(CONFIG_GPS_MODE_UE_BASED, mode);
    TEST_ASSERT_EQUAL(ESP_OK, config_get_expiration(&u32));
    TEST_ASSERT_EQUAL_UINT32(31536000, u32);
}

TEST_CASE("init never overwrites an existing value", TEST_TAG) {
    uint32_t u32;
    config_gps_mode_t mode;

    TEST_ASSERT_EQUAL(ESP_OK, config_init());
    TEST_ASSERT_EQUAL(ESP_OK, config_set_keep_alive(120));
    TEST_ASSERT_EQUAL(ESP_OK, config_set_gps_mode(CONFIG_GPS_MODE_STANDALONE));

    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    TEST_ASSERT_EQUAL(ESP_OK, config_get_keep_alive(&u32));
    TEST_ASSERT_EQUAL_UINT32(120, u32);
    TEST_ASSERT_EQUAL(ESP_OK, config_get_gps_mode(&mode));
    TEST_ASSERT_EQUAL(CONFIG_GPS_MODE_STANDALONE, mode);
}

TEST_CASE("settings without a default stay unconfigured", TEST_TAG) {
    char buffer[PEM_BUFFER_LEN];

    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, config_get_broker_url(buffer, sizeof(buffer)));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, read_certificate(buffer));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, read_ca(buffer));
}

TEST_CASE("pem getters return an allocated copy and accept a null error pointer", TEST_TAG) {
    esp_err_t err = ESP_FAIL;

    TEST_ASSERT_NULL(config_get_certificate(&err));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, err);
    TEST_ASSERT_NULL(config_get_ca(&err));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, err);
    TEST_ASSERT_NULL(config_get_ca(nullptr));

    TEST_ASSERT_EQUAL(ESP_OK, config_set_ca(TEST_CERT));
    char *ca = config_get_ca(nullptr);
    TEST_ASSERT_NOT_NULL(ca);
    TEST_ASSERT_EQUAL_STRING(TEST_CERT, ca);
    free(ca);

    char *csr = config_get_csr(&err);
    TEST_ASSERT_NOT_NULL(csr);
    TEST_ASSERT_EQUAL(ESP_OK, err);
    free(csr);
}

// --- connection and gps ---

TEST_CASE("connection and gps settings are written and read back", TEST_TAG) {
    char url[CONFIG_BROKER_URL_MAX_LEN];
    uint32_t u32;
    uint8_t u8;

    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    TEST_ASSERT_EQUAL(ESP_OK, config_set_broker_url("mqtts://broker.example.com:8883"));
    TEST_ASSERT_EQUAL(ESP_OK, config_get_broker_url(url, sizeof(url)));
    TEST_ASSERT_EQUAL_STRING("mqtts://broker.example.com:8883", url);

    TEST_ASSERT_EQUAL(ESP_OK, config_set_qos(2));
    TEST_ASSERT_EQUAL(ESP_OK, config_get_qos(&u8));
    TEST_ASSERT_EQUAL_UINT8(2, u8);

    TEST_ASSERT_EQUAL(ESP_OK, config_set_gps_timeout(15));
    TEST_ASSERT_EQUAL(ESP_OK, config_get_gps_timeout(&u32));
    TEST_ASSERT_EQUAL_UINT32(15, u32);
}

TEST_CASE("invalid values are refused and nothing changes", TEST_TAG) {
    uint32_t u32;
    uint8_t u8;

    TEST_ASSERT_EQUAL(ESP_OK, config_init());

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set_qos(3));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set_keep_alive(0));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set_gps_mode((config_gps_mode_t) 7));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set_broker_url(nullptr));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set_broker_url(""));

    TEST_ASSERT_EQUAL(ESP_OK, config_get_qos(&u8));
    TEST_ASSERT_EQUAL_UINT8(0, u8);
    TEST_ASSERT_EQUAL(ESP_OK, config_get_keep_alive(&u32));
    TEST_ASSERT_EQUAL_UINT32(60, u32);
}

TEST_CASE("a read into a buffer that is too small reports the size", TEST_TAG) {
    char small[4];

    TEST_ASSERT_EQUAL(ESP_OK, config_set_broker_url("mqtts://broker.example.com"));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_SIZE, config_get_broker_url(small, sizeof(small)));
}

// --- authentication ---

TEST_CASE("csr is created on the first read and stays the same", TEST_TAG) {
    char first[PEM_BUFFER_LEN];
    char second[PEM_BUFFER_LEN];

    TEST_ASSERT_EQUAL(ESP_OK, read_csr(first));
    TEST_ASSERT_EQUAL(ESP_OK, read_csr(second));
    TEST_ASSERT_EQUAL_STRING(first, second);
}

TEST_CASE("installing a certificate removes the csr and refuses new reads", TEST_TAG) {
    char buffer[PEM_BUFFER_LEN];

    TEST_ASSERT_EQUAL(ESP_OK, read_csr(buffer));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set_certificate("not a certificate"));
    TEST_ASSERT_EQUAL(ESP_OK, read_csr(buffer));

    TEST_ASSERT_EQUAL(ESP_OK, config_set_certificate(TEST_CERT));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_STATE, read_csr(buffer));

    TEST_ASSERT_EQUAL(ESP_OK, read_certificate(buffer));
    TEST_ASSERT_EQUAL_STRING(TEST_CERT, buffer);
}

TEST_CASE("an interrupted install still returns the stored csr", TEST_TAG) {
    char csr[PEM_BUFFER_LEN];
    char again[PEM_BUFFER_LEN];

    TEST_ASSERT_EQUAL(ESP_OK, read_csr(csr));

    // Leave both the CSR and the certificate in storage, as an interruption between the two steps would
    const storage_ctx_t cert_ctx = STORAGE_RW(SECURITY_NAMESPACE, SECURITY_CERT_KEY);
    TEST_ASSERT_EQUAL(ESP_OK, storage_set_str(&cert_ctx, TEST_CERT));

    TEST_ASSERT_EQUAL(ESP_OK, read_csr(again));
    TEST_ASSERT_EQUAL_STRING(csr, again);
}

TEST_CASE("the ca is replaced, validated and kept on revoke", TEST_TAG) {
    char buffer[PEM_BUFFER_LEN];

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set_ca("not a certificate"));
    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_set_ca(TEST_CERT TEST_CERT));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, read_ca(buffer));

    TEST_ASSERT_EQUAL(ESP_OK, config_set_ca(TEST_CERT));
    TEST_ASSERT_EQUAL(ESP_OK, config_command_revoke(1));

    TEST_ASSERT_EQUAL(ESP_OK, read_ca(buffer));
    TEST_ASSERT_EQUAL_STRING(TEST_CERT, buffer);
}

TEST_CASE("revoke removes the credentials and only accepts 0 and 1", TEST_TAG) {
    char buffer[PEM_BUFFER_LEN];

    TEST_ASSERT_EQUAL(ESP_OK, read_csr(buffer));
    TEST_ASSERT_EQUAL(ESP_OK, config_set_certificate(TEST_CERT));

    TEST_ASSERT_EQUAL(ESP_ERR_INVALID_ARG, config_command_revoke(2));
    TEST_ASSERT_EQUAL(ESP_OK, config_command_revoke(0));
    TEST_ASSERT_EQUAL(ESP_OK, read_certificate(buffer));

    TEST_ASSERT_EQUAL(ESP_OK, config_command_revoke(1));
    TEST_ASSERT_EQUAL(ESP_ERR_NOT_FOUND, read_certificate(buffer));

    // A new request can be created once the credentials are revoked
    TEST_ASSERT_EQUAL(ESP_OK, read_csr(buffer));
}

TEST_CASE("status follows the credentials", TEST_TAG) {
    config_cert_status_t status;
    char buffer[PEM_BUFFER_LEN];

    TEST_ASSERT_EQUAL(ESP_OK, config_get_status(&status));
    TEST_ASSERT_EQUAL(CONFIG_CERT_STATUS_PENDING, status);

    TEST_ASSERT_EQUAL(ESP_OK, read_csr(buffer));
    TEST_ASSERT_EQUAL(ESP_OK, config_set_certificate(TEST_CERT));
    TEST_ASSERT_EQUAL(ESP_OK, config_get_status(&status));
    TEST_ASSERT_EQUAL(CONFIG_CERT_STATUS_VALID, status);

    TEST_ASSERT_EQUAL(ESP_OK, config_command_revoke(1));
    TEST_ASSERT_EQUAL(ESP_OK, config_get_status(&status));
    TEST_ASSERT_EQUAL(CONFIG_CERT_STATUS_REVOKED, status);

    TEST_ASSERT_EQUAL(ESP_OK, read_csr(buffer));
    TEST_ASSERT_EQUAL(ESP_OK, config_get_status(&status));
    TEST_ASSERT_EQUAL(CONFIG_CERT_STATUS_PENDING, status);
}

TEST_CASE("status is pending when a request is stored after an interrupted revoke", TEST_TAG) {
    config_cert_status_t status;
    char buffer[PEM_BUFFER_LEN];

    TEST_ASSERT_EQUAL(ESP_OK, config_command_revoke(1));

    // Create the request behind the config layer's back, so the revoked marker is still set
    TEST_ASSERT_EQUAL(ESP_OK, security_create_csr());
    TEST_ASSERT_EQUAL(ESP_OK, read_csr(buffer));

    TEST_ASSERT_EQUAL(ESP_OK, config_get_status(&status));
    TEST_ASSERT_EQUAL(CONFIG_CERT_STATUS_PENDING, status);
}
