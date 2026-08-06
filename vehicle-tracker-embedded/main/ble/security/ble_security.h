#ifndef BLE_SECURITY_H

#define BLE_SECURITY_H

#include <stdint.h>

/**
 * @brief Configures the NimBLE Security Manager (bonding, MITM protection, secure connections
 * and key distribution) in ble_hs_cfg. Must be called before ble_manager_init() starts advertising.
 */
void ble_security_init(void);

/**
 * @brief Generates a random 6-digit passkey (100000-999999) to be displayed to the user and
 * entered on the peer device during pairing.
 * @return The generated passkey.
 */
uint32_t ble_security_generate_passkey();

#endif
