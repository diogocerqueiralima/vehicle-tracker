#ifndef SECURITY_H

#define SECURITY_H

#include "esp_err.h"
#include "storage.h"
#include "mbedtls/x509_crt.h"
#include "psa/crypto_types.h"
#include "uuid.h"

/** Size of the device identity in bytes. The identity is a UUIDv4, which is 16 bytes long. */
#define SECURITY_IDENTITY_MAX_LEN   UUID_LEN
#define SECURITY_NAMESPACE          "security"
#define SECURITY_IDENTITY_KEY       "identity"

#define SECURITY_CSR_KEY            "csr"
#define SECURITY_CERT_KEY           "cert"
#define SECURITY_CA_KEY             "ca"

/**
 *
 * @brief Initializes the security layer. Must be called after storage_init().
 *
 * This function initializes the PSA Crypto library and ensures that a device identity exists in the storage layer.
 *
 * Generates the device identity if it does not exist, and stores it in the storage layer.
 * The identity is a UUIDv4 that is stored in the storage layer as bytes.
 *
 * Rebuilds the global CA store from the CA certificates stored in the storage layer. If none are stored, the store is empty.
 * Calling this function again rebuilds the store, so it does not install a certificate twice.
 *
 * If the stored CA certificate cannot be installed, it is not valid and cannot be used, so it is erased from the storage
 * layer and the function returns ESP_FAIL. The device should be restarted, and the next call starts with an empty CA store,
 * ready to receive a new CA certificate. Any identity generated during the call has already been stored by then.
 *
 * @return ESP_OK on success, ESP_FAIL if the stored CA certificate cannot be installed, or an appropriate error code
 * if the crypto library cannot be initialized or the device identity cannot be read, generated or stored.
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
 * Each request generates a new private key in the PSA key store, replacing the key of any previous request.
 *
 * @return ESP_OK on success, ESP_ERR_INVALID_STATE if a CSR or a certificate is already stored,
 * or an appropriate error code on failure.
 */
esp_err_t security_create_csr();

/**
 *
 * @brief Installs the device certificate issued for the CSR. The certificate is stored first, then the stored CSR is deleted.
 *
 * An interruption between the two steps leaves both in storage, and the stored CSR is the one the certificate was issued for.
 * The same happens if the CSR cannot be deleted: the certificate is installed, the failure is only logged, and the
 * function returns ESP_OK. The leftover CSR is removed the next time the credentials are revoked.
 *
 * The certificate is checked to be valid PEM before it is stored, so an invalid one never reaches the storage layer.
 * It is not checked against the private key or the CA certificate, so a certificate issued for another key is accepted.
 *
 * @param certificate the certificate as a null-terminated string in PEM format. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG if the argument is NULL or not a valid certificate, or an appropriate error code on failure.
 */
esp_err_t security_install_certificate(const char *certificate);

/**
 *
 * @brief Sets the CA certificate the device trusts. The certificate is kept in the storage layer, so it survives a reset.
 *
 * The device holds a single CA certificate, so a call replaces the one already stored.
 * The certificate must hold exactly one certificate, and it is validated before it is stored, so an invalid certificate never reaches the storage layer.
 * Revoking the credentials does not remove the CA certificate.
 *
 * @param ca_certificate the CA certificate as a null-terminated string in PEM format. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG if the argument is NULL or not a valid certificate, or an appropriate error code on failure.
 */
esp_err_t security_set_ca_certificate(const char *ca_certificate);

/**
 *
 * @brief Checks whether the CA certificate is installed, which means exactly one certificate is both in the global CA store and in the storage layer.
 *
 * @return ESP_OK if the certificate is in both, ESP_ERR_NOT_FOUND if it is in neither,
 * ESP_ERR_INVALID_STATE if it is in only one of them or the global store holds more than one certificate, or the storage error if the storage layer cannot be read.
 */
esp_err_t security_has_ca_certificate();

/**
 *
 * @brief Gets the size of the buffer needed to read the Certificate Signing Request (CSR), in bytes including the null terminator.
 *
 * Unlike a read that returns NULL, this tells a CSR that is not stored apart from a failure.
 *
 * @param out_len the pointer to the variable that receives the size. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the CSR is not stored, ESP_ERR_INVALID_ARG if the argument is NULL,
 * or an appropriate error code on failure.
 */
esp_err_t security_get_csr_size(size_t *out_len);

/**
 *
 * @brief Reads the Certificate Signing Request (CSR) from the storage layer into a caller buffer, as a null-terminated string in PEM format.
 *
 * @param out_value the buffer that receives the CSR. Must not be NULL.
 * @param max_len the size of the buffer in bytes, as reported by the matching size function.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the CSR is not stored, ESP_ERR_INVALID_ARG if the buffer is NULL,
 * ESP_ERR_INVALID_SIZE if the buffer is too small, or an appropriate error code on failure.
 */
esp_err_t security_read_csr(char *out_value, size_t max_len);

/**
 *
 * @brief Gets the size of the buffer needed to read the installed device certificate, in bytes including the null terminator.
 *
 * @param out_len the pointer to the variable that receives the size. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the certificate is not stored, ESP_ERR_INVALID_ARG if the argument is NULL,
 * or an appropriate error code on failure.
 */
esp_err_t security_get_certificate_size(size_t *out_len);

/**
 *
 * @brief Reads the installed device certificate from the storage layer into a caller buffer, as a null-terminated string in PEM format.
 *
 * @param out_value the buffer that receives the certificate. Must not be NULL.
 * @param max_len the size of the buffer in bytes, as reported by the matching size function.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the certificate is not stored, ESP_ERR_INVALID_ARG if the buffer is NULL,
 * ESP_ERR_INVALID_SIZE if the buffer is too small, or an appropriate error code on failure.
 */
esp_err_t security_read_certificate(char *out_value, size_t max_len);

/**
 *
 * @brief Gets the size of the buffer needed to read the CA certificate, in bytes including the null terminator.
 *
 * @param out_len the pointer to the variable that receives the size. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the CA certificate is not stored, ESP_ERR_INVALID_ARG if the argument is NULL,
 * or an appropriate error code on failure.
 */
esp_err_t security_get_ca_certificate_size(size_t *out_len);

/**
 *
 * @brief Reads the CA certificate from the storage layer into a caller buffer, as a null-terminated string in PEM format.
 *
 * @param out_value the buffer that receives the CA certificate. Must not be NULL.
 * @param max_len the size of the buffer in bytes, as reported by the matching size function.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when the CA certificate is not stored, ESP_ERR_INVALID_ARG if the buffer is NULL,
 * ESP_ERR_INVALID_SIZE if the buffer is too small, or an appropriate error code on failure.
 */
esp_err_t security_read_ca_certificate(char *out_value, size_t max_len);

/**
 *
 * @brief Checks whether the installed device certificate has expired.
 *
 * The certificate is read from the storage layer and its end of validity is compared with the system clock.
 * The clock must be synchronized before this function is called. The function does not check it, so with an
 * unsynchronized clock the result is not reliable.
 *
 * @param expired receives true when the certificate has expired. Must not be NULL.
 * @return ESP_OK on success, ESP_ERR_NOT_FOUND when no certificate is installed, ESP_ERR_INVALID_ARG if the argument
 * is NULL, ESP_ERR_INVALID_STATE if the stored certificate cannot be parsed, or an appropriate error code on failure.
 */
esp_err_t security_certificate_is_expired(bool *expired);

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
