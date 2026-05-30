#include "ble_manager.h"

#include "host/ble_gatt.h"
#include "services/gatt/ble_svc_gatt.h"

void ble_manager_init() {

    // 1. Initialize GATT Service
    ble_svc_gatt_init();


}
