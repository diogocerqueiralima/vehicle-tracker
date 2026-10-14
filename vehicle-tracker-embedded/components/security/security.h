#ifndef SECURITY_H

#define SECURITY_H

#include "esp_err.h"
#include "storage.h"
#include "psa/crypto_types.h"

/** Size of the device identity in bytes. The identity is a UUIDv4, which is 16 bytes long. */
#define SECURITY_IDENTITY_MAX_LEN   16
#define SECURITY_NAMESPACE          "security"
#define SECURITY_IDENTITY_KEY       "identity"

#define SECURITY_CSR_KEY            "csr"
#define SECURITY_CERT_KEY           "cert"

static const storage_ctx_t identity_ctx = STORAGE_RW(SECURITY_NAMESPACE, SECURITY_IDENTITY_KEY);
static const storage_ctx_t csr_ctx      = STORAGE_RW(SECURITY_NAMESPACE, SECURITY_CSR_KEY);
static const storage_ctx_t cert_ctx     = STORAGE_RW(SECURITY_NAMESPACE, SECURITY_CERT_KEY);

/**
 *
 * @brief Initializes the security layer. Must be called after storage_init().
 *
 * This function initializes the PSA Crypto library and ensures that a device identity exists in the storage layer.
 *
 * Generates the device identity if it does not exist, and stores it in the storage layer.
 * The identity is a UUIDv4 that is stored in the storage layer as bytes.
 *
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t security_init();

/**
 *
 * @brief Retrieves the device identity from the storage layer. The identity is a UUIDv4 and is 16 bytes long.
 * The caller must provide a buffer of at least 16 bytes to receive the identity.
 *
 * @param identity the buffer to receive the device identity. Must be at least 16 bytes long.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t security_get_identity(uint8_t *identity);

/**
 *
 * @brief Retrieves the private key from the PSA key store. If the key does not exist, it will return an error.
 *
 * @param key_id the pointer to the variable that will receive the key ID of the private key. Must not be NULL.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t security_get_private_key(psa_key_id_t *key_id);

/**
 *
 * @brief Creates a Certificate Signing Request (CSR) using the device's private key and identity as the subject.
 * The CSR is generated (in PEM format) and stored in the storage layer under a predefined key.
 *
 * The CSR is only generated if there is no existing CSR and certificate already stored.
 * If a CSR already exists, this function will return a error without generating a new one.
 * Each request generates a new private key in the PSA key store, replacing the key of any previous request.
 *
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t security_create_csr();

/**
 *
 * @brief Retrieves the Certificate Signing Request (CSR) from the storage layer.
 * The CSR is returned as a null-terminated string in PEM format.
 *
 * In case of an error, the function returns NULL. The caller is responsible for freeing the returned string.
 *
 * @return the CSR as a null-terminated string in PEM format, or NULL on error.
 */
char *security_get_csr();

/**
 *
 * @brief Revokes the device credentials by deleting the certificate, the CSR and the private key, if they exist.
 *
 * The certificate is deleted first, then the CSR, then the private key. Missing items are not an error.
 *
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t security_revoke();

#endif
