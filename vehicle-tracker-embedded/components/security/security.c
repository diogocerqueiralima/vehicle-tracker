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

    // 2. Make sure the device is in the UNPROVISIONED state
    const security_state_t current_state = security_context->state;
    if (current_state != SECURITY_STATE_UNPROVISIONED) {
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
    security_context->state = SECURITY_STATE_ENROLLING;
    err = storage_set_u32(&STATE_CTX, SECURITY_STATE_ENROLLING);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save state");
        return err;
    }

    // 8. Enrollment started successfully
    return ESP_OK;
}