#ifndef AUTHENTICATION_SERVICE_H

#define AUTHENTICATION_SERVICE_H

#define CSR_NAMESPACE                           "csr"
#define CERTIFICATE_NAMESPACE                   "certificate"
#define CA_NAMESPACE                            "ca"
#define REVOKE_NAMESPACE                        "revoke"
#define EXPIRATION_NAMESPACE                    "expiration"
#define STATUS_NAMESPACE                        "status"

#include "host/ble_gatt.h"

/**
 * @brief GATT service definition for the authentication service.
 * Pass this to ble_manager_register_service() before calling ble_manager_init().
 */
extern const struct ble_gatt_svc_def authentication_service_def;

#endif
