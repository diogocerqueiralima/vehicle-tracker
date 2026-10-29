#include "security.h"

#include <inttypes.h>
#include <pthread.h>

#include "crypto.h"
#include "esp_log.h"
#include "nvs.h"
#include "storage.h"
#include "uuid.h"

static security_context_t *security_context = nullptr;

/**
 * @brief Mutex that protects the access to the security context.
 */
static pthread_mutex_t security_mutex = PTHREAD_MUTEX_INITIALIZER;

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
 * @brief Storage context for the PEM encoded certificate authority.
 */
#define CA_CTX ((storage_ctx_t){.namespace = "security", .key = "ca", .mode = STORAGE_MODE_READ_WRITE})

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
 * @brief Loads the lifecycle state saved in storage.
 *
 * The state is kept in storage as a 32-bit unsigned integer. It is read as one and checked against the known states
 * before being converted, so a corrupted value, or one written by a newer firmware, is never used as a state.
 *
 * @param out_state the state to be filled. It is only set on success.
 * @return ESP_OK on success, ESP_ERR_NVS_NOT_FOUND when no state is saved, ESP_ERR_INVALID_STATE when the saved value
 * is not a known state, or an appropriate error code on failure.
 */
static esp_err_t load_state(security_state_t *out_state) {

    // 1. Read the raw value
    uint32_t stored_state = 0;
    const esp_err_t err = storage_get_u32(&STATE_CTX, &stored_state);
    if (err != ESP_OK) {
        return err;
    }

    // 2. Make sure it is a known state
    if (stored_state > SECURITY_STATE_REVOKED) {
        ESP_LOGE(TAG, "Stored state %" PRIu32 " is not a valid state", stored_state);
        return ESP_ERR_INVALID_STATE;
    }

    // 3. Convert it
    *out_state = (security_state_t) stored_state;
    return ESP_OK;
}

/**
 *
 * @brief Makes sure the device has a lifecycle state, loading it from storage or starting as UNPROVISIONED
 * when none is saved. The state is kept in storage as a 32-bit unsigned integer, so it stays the same across reboots.
 *
 * @return ESP_OK when the device has a lifecycle state, or an appropriate error code on failure, such as
 * ESP_ERR_INVALID_STATE when the saved state is not a known one.
 */
static esp_err_t ensure_state() {

    // 1. Load the saved state, if there is one
    security_state_t state;
    const esp_err_t err = load_state(&state);
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

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer was not initialized yet, otherwise the first context would be leaked
    esp_err_t err = ESP_OK;
    if (security_context != nullptr) {
        ESP_LOGE(TAG, "Security layer is already initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 2. Allocate the security context, zeroed so that no field is left uninitialized
    security_context = calloc(1, sizeof(security_context_t));
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate security context");
        err = ESP_ERR_NO_MEM;
        goto unlock;
    }

    // 3. Make sure the device has an identity
    err = ensure_identity();
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to create identity context");
        free(security_context);
        security_context = nullptr;
        goto unlock;
    }

    // 4. Make sure the device has a lifecycle state
    err = ensure_state();
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to create state context");
        free(security_context);
        security_context = nullptr;
    }

unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}

esp_err_t security_get_context(security_context_t *context) {

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer is initialized
    esp_err_t err = ESP_OK;
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 2. Copy the context to the provided pointer, so the caller gets a snapshot
    *context = *security_context;

unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}

esp_err_t security_start_enrollment() {

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer is initialized
    esp_err_t err = ESP_OK;
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 2. Make sure the device is in a state that allows enrollment: UNPROVISIONED,
    // or EXPIRED and REVOKED to enroll again
    const security_state_t current_state = security_context->state;
    if (
        current_state != SECURITY_STATE_UNPROVISIONED && current_state != SECURITY_STATE_EXPIRED &&
        current_state != SECURITY_STATE_REVOKED
    ) {
        ESP_LOGE(TAG, "Device is not in the correct state for enrollment");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 3. Generate the private key, which replaces the old one, and that is harmless if a later step fails because
    // the old key is useless in these states
    err = crypto_generate_private_key(PRIVATE_KEY_ID);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to generate private key");
        goto unlock;
    }

    // 4. Get the device identity as a string, to use it as the common name in the CSR
    char identity[UUID_STR_SIZE];
    err = uuid_to_str(&security_context->identity, identity, sizeof(identity));
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to convert identity to string");
        goto unlock;
    }

    // 5. Generate the CSR, signed with the private key
    char csr[CSR_MAX_SIZE];
    err = crypto_generate_csr(PRIVATE_KEY_ID, identity, csr, sizeof(csr));
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to generate CSR");
        goto unlock;
    }

    // 6. Save the CSR to storage
    err = storage_set_str(&CSR_CTX, csr);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save CSR");
        goto unlock;
    }

    // 7. Transition to the ENROLLING state
    err = storage_set_u32(&STATE_CTX, SECURITY_STATE_ENROLLING);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save state");
        goto unlock;
    }

    security_context->state = SECURITY_STATE_ENROLLING;

    // 8. Enrollment started successfully
unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}

esp_err_t security_install_certificate(const char *cert_pem) {

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer is initialized
    esp_err_t err = ESP_OK;
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 2. Make sure the device is in the ENROLLING state
    const security_state_t current_state = security_context->state;
    if (current_state != SECURITY_STATE_ENROLLING) {
        ESP_LOGE(TAG, "Device is not in the correct state for certificate installation");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 3. Parse the certificate
    mbedtls_x509_crt cert;
    err = crypto_parse_certificate(cert_pem, &cert);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to parse certificate");
        goto unlock;
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

    // 7. Delete the CSR, it is not needed anymore, and it is fine if there is none. If this fails, the device can still
    // operate, so we log the error but continue to transition to ACTIVE
    const esp_err_t csr_err = storage_erase(&CSR_CTX);
    if (csr_err != ESP_OK && csr_err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to delete CSR: %s", esp_err_to_name(csr_err));
    }

    // 8. Transition to the ACTIVE state
    err = storage_set_u32(&STATE_CTX, SECURITY_STATE_ACTIVE);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save state");
        goto cleanup;
    }

    security_context->state = SECURITY_STATE_ACTIVE;

cleanup:
    mbedtls_x509_crt_free(&cert);

unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}

esp_err_t security_revoke() {

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer is initialized
    esp_err_t err = ESP_OK;
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 2. Make sure the device has a certificate to revoke, which is the case in the ACTIVE and RENEWING states
    const security_state_t current_state = security_context->state;
    if (current_state != SECURITY_STATE_ACTIVE && current_state != SECURITY_STATE_RENEWING) {
        ESP_LOGE(TAG, "Device is not in the correct state for revocation");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 3. Delete the private key
    err = crypto_destroy_private_key(PRIVATE_KEY_ID);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to delete private key");
        goto unlock;
    }

    // 4. Delete the certificate, it is fine if there is none
    err = storage_erase(&CERT_CTX);
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to delete certificate");
        goto unlock;
    }

    // 5. Delete the CSR, it is fine if there is none
    err = storage_erase(&CSR_CTX);
    if (err != ESP_OK && err != ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGE(TAG, "Failed to delete CSR");
        goto unlock;
    }

    // 6. Transition to the REVOKED state, only after everything was deleted
    err = storage_set_u32(&STATE_CTX, SECURITY_STATE_REVOKED);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save state");
        goto unlock;
    }

    security_context->state = SECURITY_STATE_REVOKED;

unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}

esp_err_t security_get_certificate(char **out_pem) {

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer is initialized
    esp_err_t err = ESP_OK;
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    if (out_pem == nullptr) {
        err = ESP_ERR_INVALID_ARG;
        goto unlock;
    }

    // 2. Make sure the device has a certificate, which is the case in the ACTIVE and RENEWING states
    const security_state_t current_state = security_context->state;
    if (current_state != SECURITY_STATE_ACTIVE && current_state != SECURITY_STATE_RENEWING) {
        ESP_LOGE(TAG, "Device is not in the correct state to have a certificate");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 3. Load the certificate from storage
    size_t size = 0;
    err = storage_get_str_size(&CERT_CTX, &size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get the certificate size");
        goto unlock;
    }

    char *pem = malloc(size);
    if (pem == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate certificate buffer");
        err = ESP_ERR_NO_MEM;
        goto unlock;
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
    err = ESP_OK;
    goto unlock;

fail:
    free(pem);

unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}

esp_err_t security_destroy() {

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer is initialized
    esp_err_t err = ESP_OK;
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 2. Release the security context, what is saved in storage is kept
    free(security_context);
    security_context = nullptr;

unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}

esp_err_t security_install_certificate_authority(const char *ca_pem) {

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer is initialized
    esp_err_t err = ESP_OK;
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    if (ca_pem == nullptr) {
        err = ESP_ERR_INVALID_ARG;
        goto unlock;
    }

    // 2. Parse the certificate authority
    mbedtls_x509_crt ca;
    err = crypto_parse_certificate(ca_pem, &ca);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to parse certificate authority");
        goto unlock;
    }

    // 3. Make sure it really is a certificate authority
    if (mbedtls_x509_crt_get_ca_istrue(&ca) != 1) {
        ESP_LOGE(TAG, "Certificate is not a certificate authority");
        err = ESP_ERR_INVALID_ARG;
        goto cleanup;
    }

    // 4. Save it to storage, replacing the previous one
    err = storage_set_str(&CA_CTX, ca_pem);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save certificate authority");
    }

cleanup:
    mbedtls_x509_crt_free(&ca);

unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}

esp_err_t security_get_certificate_authority(char **out_pem) {

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer is initialized
    esp_err_t err = ESP_OK;
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    if (out_pem == nullptr) {
        err = ESP_ERR_INVALID_ARG;
        goto unlock;
    }

    // 2. Get the size of the certificate authority saved in storage
    size_t size = 0;
    err = storage_get_str_size(&CA_CTX, &size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get the certificate authority size");
        goto unlock;
    }

    // 3. Load it
    char *pem = malloc(size);
    if (pem == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate certificate authority buffer");
        err = ESP_ERR_NO_MEM;
        goto unlock;
    }

    err = storage_get_str(&CA_CTX, pem, size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to load certificate authority");
        free(pem);
        goto unlock;
    }

    *out_pem = pem;

unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}

esp_err_t security_get_csr(char **out_pem) {

    pthread_mutex_lock(&security_mutex);

    // 1. Make sure the security layer is initialized
    esp_err_t err = ESP_OK;
    if (security_context == nullptr) {
        ESP_LOGE(TAG, "Security layer is not initialized");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    if (out_pem == nullptr) {
        err = ESP_ERR_INVALID_ARG;
        goto unlock;
    }

    // 2. Make sure the device has a CSR, which is the case in the ENROLLING state
    if (security_context->state != SECURITY_STATE_ENROLLING) {
        ESP_LOGE(TAG, "Device is not in the correct state to have a CSR");
        err = ESP_ERR_INVALID_STATE;
        goto unlock;
    }

    // 3. Get the size of the CSR saved in storage
    size_t size = 0;
    err = storage_get_str_size(&CSR_CTX, &size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get the CSR size");
        goto unlock;
    }

    // 4. Load it
    char *pem = malloc(size);
    if (pem == nullptr) {
        ESP_LOGE(TAG, "Failed to allocate CSR buffer");
        err = ESP_ERR_NO_MEM;
        goto unlock;
    }

    err = storage_get_str(&CSR_CTX, pem, size);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to load CSR");
        free(pem);
        goto unlock;
    }

    *out_pem = pem;

unlock:
    pthread_mutex_unlock(&security_mutex);
    return err;
}
