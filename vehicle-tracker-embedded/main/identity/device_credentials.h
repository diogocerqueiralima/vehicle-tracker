#ifndef DEVICE_CREDENTIALS_H

#define DEVICE_CREDENTIALS_H

#include <stdbool.h>
#include <stddef.h>
#include "esp_err.h"
#include "psa/crypto.h"

/**
 * @brief Returns the identifier of the device's private key, generating the key pair on first call.
 *
 * The key is a NIST P-256 (secp256r1) signing key held in PSA's persistent key store, which keeps
 * the key material in NVS. It is created without export permission, so callers only ever get this
 * reference and the private key never reaches an application buffer. Storage must already be
 * initialized, as PSA loads its key store from NVS.
 *
 * @param out_key_id Output parameter that will be set to the key identifier.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t device_credentials_get_private_key(psa_key_id_t *out_key_id);

/**
 * @brief Erases the device's private key from PSA's persistent key store, wiping the key material.
 *
 * Any certificate issued for the key stops being usable, as the device can no longer prove it holds
 * the matching private key. A device with no key is back to its first-boot state: the next call to
 * device_credentials_get_private_key() or device_credentials_generate_csr() creates a new key pair.
 *
 * @return ESP_OK on success, including when there is no key to erase, or an appropriate error code
 * on failure.
 */
esp_err_t device_credentials_delete_private_key();

/**
 * @brief Reports whether the device currently holds a certificate.
 *
 * @param err Pointer to an esp_err_t variable to receive the error code. Must not be NULL. Set to
 * ESP_OK once the check has completed, regardless of the result, or to the failure reported by
 * storage otherwise, in which case the return value must not be relied upon.
 * @return true when a certificate is stored, false when there is none or the check failed.
 */
bool device_credentials_has_certificate(esp_err_t *err);

/**
 * @brief Generates a new key pair for the device and a PEM-encoded certificate signing request (CSR)
 * for it, signed by that key pair. The common name is the device's identifier.
 *
 * Requesting a CSR is how the user (re-)enrolls a device whose private key may be compromised, so any
 * previous key pair is destroyed and replaced, and the device stays unable to authenticate until the
 * certificate issued for this request is installed with device_credentials_save_certificate().
 *
 * Refuses to run while a certificate is already installed, since rotating the key would strand that
 * certificate without a matching key. The certificate must first be removed, e.g. via revocation,
 * before a new CSR can be requested.
 *
 * @param err Pointer to an esp_err_t variable to receive the error code. Must not be NULL.
 * @return Pointer to a dynamically allocated, null-terminated string containing the PEM-encoded
 * CSR, whose length the caller can measure with strlen(). The caller is responsible for freeing
 * this memory. Returns NULL on failure, including ESP_ERR_INVALID_STATE when a certificate is
 * already installed, and sets *err to the appropriate error code.
 */
char *device_credentials_generate_csr(esp_err_t *err);

/**
 * @brief Persists the certificate the backend issued for this device, replacing any previous one.
 *
 * @param pem PEM-encoded certificate string. Must be null-terminated.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t device_credentials_save_certificate(const char *pem);

/**
 * @brief Deletes the certificate stored for this device, if there is one.
 *
 * @return ESP_OK on success, including when no certificate is stored, or an appropriate error code
 * on failure.
 */
esp_err_t device_credentials_delete_certificate();

/**
 * @brief Loads the certificate issued for this device.
 *
 * @param err Pointer to an esp_err_t variable to receive the error code. It's set to ESP_OK on
 * success, to ESP_ERR_NOT_FOUND when the device holds no certificate, and to the failure reported by
 * storage otherwise. A caller deciding whether the device needs to be enrolled must look for
 * ESP_ERR_NOT_FOUND specifically, as any other error leaves it unknown whether a certificate exists.
 * @return Pointer to a dynamically allocated, null-terminated string holding the PEM-encoded
 * certificate. The caller is responsible for freeing this memory. Returns NULL when no certificate
 * is stored yet or on failure, and sets *err to the appropriate error code.
 */
char *device_credentials_load_certificate(esp_err_t *err);

#endif
