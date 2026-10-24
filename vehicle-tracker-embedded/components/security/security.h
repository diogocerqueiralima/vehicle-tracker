#ifndef SECURITY_H

#define SECURITY_H

#include "esp_err.h"
#include "uuid.h"

/**
 * @brief Lifecycle state of the device credentials. It decides what the device allows, such as when it creates a new CSR.
 */
typedef enum
{
    /** The device has no certificate stored. It cannot authenticate with other services. */
    SECURITY_STATE_UNPROVISIONED,

    /** The device has generated its CSR and is waiting for the user to install the certificate. */
    SECURITY_STATE_ENROLLING,

    /** The device has a valid certificate. It can authenticate with other services. */
    SECURITY_STATE_ACTIVE,

    /** The device has requested a new certificate and is waiting for the result. The current certificate is still valid. */
    SECURITY_STATE_RENEWING,

    /** The certificate expired before the renewal succeeded. The device must be enrolled again. */
    SECURITY_STATE_EXPIRED,

    /** The certificate was revoked. The device must be enrolled again. */
    SECURITY_STATE_REVOKED
} security_state_t;

typedef struct {
    uuid_t identity;
    security_state_t state;
} security_context_t;

/**
 *
 * @brief Initializes the security layer, preparing the device to authenticate with other services.
 *
 * Storage must be initialized before calling this function. On the first boot it generates the device identity and
 * saves it, so it stays the same across reboots. On every boot it loads the lifecycle state saved in flash, or starts
 * as UNPROVISIONED when none is saved.
 *
 * This function creates or loads the security context, which contains the device identity and lifecycle state.
 *
 * @return ESP_OK on successful initialization, ESP_ERR_INVALID_STATE when storage is not initialized, or an
 * appropriate error code on failure.
 */
esp_err_t security_init();

/**
 *
 * @brief Gets the security context, which contains the device identity and lifecycle state.
 *
 * @param context the pointer to the security context to be filled. It must be allocated by the caller.
 * @return ESP_OK on success or ESP_ERR_INVALID_STATE when the security layer is not initialized.
 */
esp_err_t security_get_context(security_context_t *context);

/**
 *
 * @brief Start the enrollment process for the device.
 *
 * This function should be called when the device is in the UNPROVISIONED state.
 * It generates a new CSR and private key, and saves them in flash. The device then transitions to the ENROLLING state,
 * waiting for the user to install the certificate. The CSR can be retrieved with security_get_csr() and the private key
 * can be retrieved with security_get_private_key().
 *
 * @return ESP_OK on success, ESP_ERR_INVALID_STATE when the device is not in the UNPROVISIONED state, or an
 * appropriate error code on failure.
 */
esp_err_t security_start_enrollment();

#endif
