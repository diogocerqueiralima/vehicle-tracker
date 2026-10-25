#ifndef CRYPTO_H

#define CRYPTO_H

#include <stddef.h>
#include "esp_err.h"
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

#endif
