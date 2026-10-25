#include "security.h"

#include "crypto.h"
#include "esp_log.h"
#include "nvs.h"
#include "storage.h"
#include "uuid.h"

static security_context_t *security_context = nullptr;

/**
 * @brief Storage context for the device identity.
 */
#define IDENTITY_CTX ((storage_ctx_t){.namespace = "security", .key = "identity", .mode = STORAGE_MODE_READ_WRITE})

/**
 * @brief Storage context for the lifecycle state of the device credentials.
 */
#define STATE_CTX ((storage_ctx_t){.namespace = "security", .key = "state", .mode = STORAGE_MODE_READ_WRITE})

/**
 * @brief Storage context for the PEM encoded CSR.
 */
#define CSR_CTX ((storage_ctx_t){.namespace = "security", .key = "csr", .mode = STORAGE_MODE_READ_WRITE})

/**
 * @brief Storage context for the PEM encoded certificate.
 */
#define CERT_CTX ((storage_ctx_t){.namespace = "security", .key = "cert", .mode = STORAGE_MODE_READ_WRITE})

/**
 * @brief Id of the device private key in the PSA key store. It is inside the range reserved for applications.
 */
#define PRIVATE_KEY_ID  PSA_KEY_ID_USER_MIN

/**
 * @brief Maximum size in bytes of the PEM encoded CSR, including the null terminator.
 *
 * The subject and the key type are fixed, so the CSR has a bounded size (around 400 bytes).
 */
#define CSR_MAX_SIZE 512

#define TAG "security"

/**
 * @brief Makes sure the device has an identity, generating and saving one on the first boot.
 *
 * The identity is a UUID v4 kept in storage as a blob of its bytes, so it stays the same across reboots.
 *
 * @return ESP_OK when the device has an identity, or an appropriate error code on failure.
 */
static esp_err_t ensure_identity() {

    // 1. Keep the identity if one is already saved
    uuid_t identity;
    esp_err_t err = storage_get_blob(&IDENTITY_CTX, identity.bytes, UUID_BYTES_SIZE);
    if (err == ESP_OK) {
        security_context->identity = identity;
        return ESP_OK;
    }

    if (err != ESP_ERR_NVS_NOT_FOUND) {
        return err;
    }

    // 2. First boot, generate a new identity
    err = uuid_generate_v4(&identity);
    if (err != ESP_OK) {
        return err;
    }

    security_context->identity = identity;

    // 3. Save it, so it never changes
    return storage_set_blob(&IDENTITY_CTX, identity.bytes, UUID_BYTES_SIZE);
}

/**
 *
 * @brief Makes sure the device has a lifecycle state, loading it from storage or starting as UNPROVISIONED
 * when none is saved. The state is kept in storage as a 32-bit unsigned integer, so it stays the same across reboots.
 *
 * @return the current lifecycle state, or an appropriate error code on failure.
 */
static esp_err_t ensure_state() {

    // 1. Keep the state if one is already saved
    security_state_t state;
    const esp_err_t err = storage_get_u32(&STATE_CTX, (uint32_t *)&state);
    if (err != ESP_ERR_NVS_NOT_FOUND && err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to load state");
        return err;
    }

    // 2. Keep the state if one is already saved
    if (err == ESP_OK) {
        security_context->state = state;
        return ESP_OK;
    }

    // 3. First boot, start as UNPROVISIONED
    security_context->state = SECURITY_STATE_UNPROVISIONED;

    // 4. Save the initial state
    return storage_set_u32(&STATE_CTX, SECURITY_STATE_UNPROVISIONED);
}

esp_err_t security_init() {

    // 1. Allocate the security context
    security_context = malloc(sizeof(security_context_t));
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate security context");
        return ESP_ERR_NO_MEM;
    }

    // 2. Make sure the device has an identity
    esp_err_t err = ensure_identity();
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to create identity context");
        free(security_context);
        return err;
    }

    // 3. Make sure the device has a lifecycle state
    err = ensure_state();
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to create state context");
        free(security_context);
        return err;
    }

    return ESP_OK;
}

esp_err_t security_get_context(security_context_t *context) {

    // 1. Make sure the security layer is initialized
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    // 2. Copy the context to the provided pointer
    *context = *security_context;

    return ESP_OK;
}

esp_err_t security_start_enrollment() {

    // 1. Make sure the security layer is initialized
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    // 2. Make sure the device is in a state that allows enrollment: UNPROVISIONED,
    // or EXPIRED and REVOKED to enroll again
    const security_state_t current_state = security_context->state;
    if (
        current_state != SECURITY_STATE_UNPROVISIONED && current_state != SECURITY_STATE_EXPIRED &&
        current_state != SECURITY_STATE_REVOKED
    ) {
        ESP_LOGE(TAG, "Device is not in the correct state for enrollment");
        return ESP_ERR_INVALID_STATE;
    }

    // 3. Generate the private key
    esp_err_t err = crypto_generate_private_key(PRIVATE_KEY_ID);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to generate private key");
        return err;
    }

    // 4. Get the device identity as a string, to use it as the common name in the CSR
    char identity[UUID_STR_SIZE];
    err = uuid_to_str(&security_context->identity, identity, sizeof(identity));
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to convert identity to string");
        return err;
    }

    // 5. Generate the CSR, signed with the private key
    char csr[CSR_MAX_SIZE];
    err = crypto_generate_csr(PRIVATE_KEY_ID, identity, csr, sizeof(csr));
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to generate CSR");
        return err;
    }

    // 6. Save the CSR to storage
    err = storage_set_str(&CSR_CTX, csr);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save CSR");
        return err;
    }

    // 7. Transition to the ENROLLING state
    err = storage_set_u32(&STATE_CTX, SECURITY_STATE_ENROLLING);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save state");
        return err;
    }

    security_context->state = SECURITY_STATE_ENROLLING;

    // 8. Enrollment started successfully
    return ESP_OK;
}

esp_err_t security_install_certificate(const char *cert_pem) {

    // 1. Make sure the security layer is initialized
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    // 2. Make sure the device is in the ENROLLING state
    const security_state_t current_state = security_context->state;
    if (current_state != SECURITY_STATE_ENROLLING) {
        ESP_LOGE(TAG, "Device is not in the correct state for certificate installation");
        return ESP_ERR_INVALID_STATE;
    }

    // 3. Parse the certificate
    mbedtls_x509_crt cert;
    esp_err_t err = crypto_parse_certificate(cert_pem, &cert);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to parse certificate");
        return err;
    }

    // 4. Make sure the certificate belongs to the device private key
    bool matches = false;
    err = crypto_certificate_matches_key(&cert, PRIVATE_KEY_ID, &matches);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to check the certificate public key");
        goto cleanup;
    }

    if (!matches) {
        ESP_LOGE(TAG, "Certificate does not match the device private key");
        err = ESP_ERR_INVALID_ARG;
        goto cleanup;
    }

    // 5. Make sure the certificate is inside its validity period
    bool valid = false;
    err = crypto_certificate_is_valid(&cert, &valid);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to check the certificate validity");
        goto cleanup;
    }

    if (!valid) {
        ESP_LOGE(TAG, "Certificate is expired or not yet valid");
        err = ESP_ERR_INVALID_ARG;
        goto cleanup;
    }

    // 6. Save the certificate to storage
    err = storage_set_str(&CERT_CTX, cert_pem);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save certificate");
        goto cleanup;
    }

    // 7. Transition to the ACTIVE state
    err = storage_set_u32(&STATE_CTX, SECURITY_STATE_ACTIVE);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save state");
        goto cleanup;
    }

    security_context->state = SECURITY_STATE_ACTIVE;

cleanup:
    mbedtls_x509_crt_free(&cert);
    return err;
}

esp_err_t security_revoke() {

    // 1. Make sure the security layer is initialized
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    // 2. Make sure the device has a certificate to revoke, which is the case in the ACTIVE and RENEWING states
    const security_state_t current_state = security_context->state;
    if (current_state != SECURITY_STATE_ACTIVE && current_state != SECURITY_STATE_RENEWING) {
        ESP_LOGE(TAG, "Device is not in the correct state for revocation");
        return ESP_ERR_INVALID_STATE;
    }

    // 3. Delete the private key
    esp_err_t err = crypto_destroy_private_key(PRIVATE_KEY_ID);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to delete private key");
        return err;
    }

    // 4. Delete the certificate, it is fine if there is none
    err = storage_erase(&CERT_CTX);
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to delete certificate");
        return err;
    }

    // 5. Delete the CSR, it is fine if there is none
    err = storage_erase(&CSR_CTX);
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to delete CSR");
        return err;
    }

    // 6. Transition to the REVOKED state, only after everything was deleted
    err = storage_set_u32(&STATE_CTX, SECURITY_STATE_REVOKED);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save state");
        return err;
    }

    security_context->state = SECURITY_STATE_REVOKED;

    return ESP_OK;
}

esp_err_t security_get_certificate(char **out_pem) {

    // 1. Make sure the security layer is initialized
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    if (out_pem == nullptr) {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Make sure the device has a certificate, which is the case in the ACTIVE and RENEWING states
    const security_state_t current_state = security_context->state;
    if (current_state != SECURITY_STATE_ACTIVE && current_state != SECURITY_STATE_RENEWING) {
        ESP_LOGE(TAG, "Device is not in the correct state to have a certificate");
        return ESP_ERR_INVALID_STATE;
    }

    // 3. Load the certificate from storage
    size_t size = 0;
    esp_err_t err = storage_get_str_size(&CERT_CTX, &size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get the certificate size");
        return err;
    }

    char *pem = malloc(size);
    if (pem == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate certificate buffer");
        return ESP_ERR_NO_MEM;
    }

    err = storage_get_str(&CERT_CTX, pem, size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to load certificate");
        goto fail;
    }

    // 4. Check if the certificate is still valid
    mbedtls_x509_crt cert;
    err = crypto_parse_certificate(pem, &cert);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to parse certificate");
        goto fail;
    }

    bool valid = false;
    err = crypto_certificate_is_valid(&cert, &valid);
    mbedtls_x509_crt_free(&cert);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to check the certificate validity");
        goto fail;
    }

    // 5. An expired certificate on an ACTIVE device starts the renewal
    if (!valid && current_state == SECURITY_STATE_ACTIVE) {
        err = storage_set_u32(&STATE_CTX, SECURITY_STATE_RENEWING);
        if (err != ESP_OK) {
            ESP_LOGE(TAG, "Failed to save state");
            goto fail;
        }

        security_context->state = SECURITY_STATE_RENEWING;

        // TODO(#232): launch the job that renews the certificate
    }

    *out_pem = pem;
    return ESP_OK;

fail:
    free(pem);
    return err;
}

