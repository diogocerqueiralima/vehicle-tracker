#include "ble_security.h"

#include "esp_random.h"
#include "host/ble_hs.h"

void ble_security_init(void)
{
    // 1. Require MITM-protected, bonded pairing so unauthenticated clients are rejected
    ble_hs_cfg.sm_io_cap = BLE_HS_IO_DISPLAY_ONLY;
    ble_hs_cfg.sm_bonding = 1;
    ble_hs_cfg.sm_mitm = 1;
    ble_hs_cfg.sm_sc = 1;

    // 2. Distribute and accept encryption/identity keys so bonds survive reconnection
    ble_hs_cfg.sm_our_key_dist |= BLE_SM_PAIR_KEY_DIST_ENC | BLE_SM_PAIR_KEY_DIST_ID;
    ble_hs_cfg.sm_their_key_dist |= BLE_SM_PAIR_KEY_DIST_ENC | BLE_SM_PAIR_KEY_DIST_ID;
}

uint32_t ble_security_generate_passkey()
{
    return 100000 + esp_random() % 900000;
}
