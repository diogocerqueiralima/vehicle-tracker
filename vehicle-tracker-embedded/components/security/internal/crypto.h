#ifndef CRYPTO_H

#define CRYPTO_H

#include <stddef.h>
#include "esp_err.h"
#include "mbedtls/x509_crt.h"
#include "psa/crypto.h"

/**
 *
 * @brief Generates a persistent ECDSA P-256 private key in the PSA key store, under the given id.
 *
 * The key can only be used for signing with ECDSA and SHA-256, and it is not exportable. The private key never leaves
 * the PSA key store, it is only used through its id. If a key already exists under the id, it is destroyed and
 * replaced by the new one, so anything signed with the old key can no longer be verified against the new public key.
 *
 * @param key_id the id the key is saved under. It must be inside the range reserved for applications.
 * @return ESP_OK when the new key was generated, or an appropriate error code on failure.
 */
esp_err_t crypto_generate_private_key(psa_key_id_t key_id);

/**
 *
 * @brief Generates a PEM encoded CSR, signed with the private key saved under the given id.
 *
 * The private key stays inside the PSA key store, the CSR is signed through its id. The output is null terminated.
 *
 * @param key_id the id of the private key used to sign the CSR. It must be an ECDSA P-256 key pair.
 * @param common_name the common name (CN) of the CSR subject.
 * @param out_pem the buffer to fill with the null terminated PEM string.
 * @param size the size of the buffer in bytes.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when an argument is null, or an appropriate error code on failure,
 * such as ESP_FAIL when the buffer is too small.
 */
esp_err_t crypto_generate_csr(psa_key_id_t key_id, const char *common_name, char *out_pem, size_t size);

/**
 *
 * @brief Parses a PEM encoded certificate.
 *
 * Checks that the certificate is well formed, it does not verify its signature, validity period or chain of trust.
 *
 * @param cert_pem the certificate in PEM format, null terminated.
 * @param out_crt the certificate to be filled. It is initialized by this function, and on success the caller must
 * release it with mbedtls_x509_crt_free(). On failure it is already released.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG when an argument is null or the PEM is not a valid certificate, or
 * an appropriate error code on failure.
 */
esp_err_t crypto_parse_certificate(const char *cert_pem, mbedtls_x509_crt *out_crt);

/**
 *
 * @brief Checks if the public key of a certificate belongs to the private key saved under the given id.
 *
 * The private key stays inside the PSA key store, only its public key is compared with the one in the certificate.
 *
 * @param crt the certificate, parsed with crypto_parse_certificate().
 * @param key_id the id of the private key.
 * @param out_matches set to true when the public keys are the same, false otherwise. It is only set on success.
 * @return ESP_OK when the check was done, ESP_ERR_INVALID_ARG when an argument is null, or an appropriate error code
 * on failure, such as when no key exists under the id.
 */
esp_err_t crypto_certificate_matches_key(const mbedtls_x509_crt *crt, psa_key_id_t key_id, bool *out_matches);

/**
 *
 * @brief Checks if a certificate is valid now, which means that the current time is inside its validity period.
 *
 * It uses the system clock, so the caller must make sure it was set (for example by SNTP) before calling it.
 *
 * @param crt the certificate, parsed with crypto_parse_certificate().
 * @param out_valid set to true when the certificate is valid now, false when it is not yet valid or expired.
 * It is only set on success.
 * @return ESP_OK when the check was done, or ESP_ERR_INVALID_ARG when an argument is null.
 */
esp_err_t crypto_certificate_is_valid(const mbedtls_x509_crt *crt, bool *out_valid);

#endif
