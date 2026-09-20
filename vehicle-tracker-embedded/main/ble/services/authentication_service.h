#ifndef AUTHENTICATION_SERVICE_H

#define AUTHENTICATION_SERVICE_H

#define CSR_NAMESPACE                           "csr"
#define CERTIFICATE_NAMESPACE                   "certificate"
#define CA_NAMESPACE                            "ca"
#define REVOKE_NAMESPACE                        "revoke"
#define EXPIRATION_NAMESPACE                    "expiration"
#define STATUS_NAMESPACE                        "status"

#include "esp_err.h"
#include "host/ble_gatt.h"

/**
 * @brief GATT service definition for the authentication service.
 * Pass this to ble_manager_register_service() before calling ble_manager_init().
 */
extern const struct ble_gatt_svc_def authentication_service_def;

/**
 * @brief Registers this service's file characteristics (CSR, certificate, CA) with gatt_common
 * for disconnect cleanup. Must be called before ble_manager_init() starts accepting connections.
 */
void authentication_service_init();

/**
 * @brief Loads the CA certificate stored under CA_NAMESPACE into esp-tls's global CA store, so
 * any TLS transport that opts in via use_global_ca_store() can validate server certificates against it.
 * If no CA is stored yet, this is a no-op.
 *
 * @return ESP_OK on success, or an error code on failure.
 */
esp_err_t authentication_service_load_ca_into_global_store();

#endif
