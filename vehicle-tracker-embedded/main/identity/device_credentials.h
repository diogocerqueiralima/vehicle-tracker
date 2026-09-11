#ifndef DEVICE_CREDENTIALS_H

#define DEVICE_CREDENTIALS_H

#include <stddef.h>
#include "esp_err.h"
#include "psa/crypto.h"

#define DEVICE_CREDENTIALS_CERTIFICATE_NVS_KEY "device_cert"

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
 * @brief Generates a PEM-encoded certificate signing request (CSR) for the device's identity,
 * signed by the device's private key. The common name is the device's identifier.
 *
 * @param err Pointer to an esp_err_t variable to receive the error code. Must not be NULL.
 * @return Pointer to a dynamically allocated, null-terminated string containing the PEM-encoded
 * CSR, whose length the caller can measure with strlen(). The caller is responsible for freeing
 * this memory. Returns NULL on failure, and sets *err to the appropriate error code.
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
 * @brief Loads the certificate issued for this device.
 *
 * @param err Pointer to an esp_err_t variable to receive the error code. Can be NULL if not needed.
 * @return Pointer to a dynamically allocated, null-terminated string holding the PEM-encoded
 * certificate. The caller is responsible for freeing this memory. Returns NULL when no certificate
 * is stored yet or on failure, and sets *err to the appropriate error code.
 */
char *device_credentials_load_certificate(esp_err_t *err);

#endif
