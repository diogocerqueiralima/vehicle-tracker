#ifndef BLE_MANAGER_H

#define BLE_MANAGER_H

#include <string.h>

#include "host/ble_hs.h"
#include "services/gatt/ble_svc_gatt.h"

typedef enum
{

    BLE_MANAGER_EVENT_PASSKEY,      // data points to a const uint32_t passkey to show to the user
    BLE_MANAGER_EVENT_CONNECTED,    // data is nullptr
    BLE_MANAGER_EVENT_DISCONNECTED, // data is nullptr

} ble_manager_event_t;

typedef void (*ble_manager_gap_cb_t)(ble_manager_event_t event, const void* data, void* arg);

/**
 * @brief Registers a callback invoked on pairing/connection GAP events (passkey display,
 * connected, disconnected). Must be called before ble_manager_init().
 * @param cb Callback to invoke, or nullptr to clear it.
 * @param arg Opaque argument passed back to the callback.
 */
void ble_manager_set_gap_callback(ble_manager_gap_cb_t cb, void* arg);

/**
 * @brief Registers a GATT service definition with the BLE manager.
 * Must be called before ble_manager_init(). The pointed-to definition must
 * remain valid for the lifetime of the BLE stack.
 * @param svc_def Pointer to the service definition to register.
 * @return 0 on success, or a non-zero error code on allocation failure.
 */
int ble_manager_register_service(const struct ble_gatt_svc_def* svc_def);

/**
 * @brief Initializes the BLE manager and starts the NimBLE host task.
 * All services must be registered via ble_manager_register_service() before calling this.
 * @return 0 on success, or an appropriate error code if initialization fails.
 */
int ble_manager_init();

#endif
