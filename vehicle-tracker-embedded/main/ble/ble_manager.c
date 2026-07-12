#include "ble_manager.h"

#include <stdlib.h>
#include "nimble/nimble_port.h"
#include "nimble/nimble_port_freertos.h"

static struct ble_gatt_svc_def* gatt_services_defs = nullptr;
static int service_count = 0;

static void ble_host_task(void* param)
{
    (void)param;
    nimble_port_run();
    nimble_port_freertos_deinit();
}

static void on_sync(void)
{
    // 1. Infer own address type (public if available, random static otherwise)
    uint8_t addr_type;
    int rc = ble_hs_id_infer_auto(0, &addr_type);
    if (rc != 0)
    {
        return;
    }

    // 2. Build advertising payload: general-discoverable, complete local name
    static constexpr char name[] = "VehicleTracker-ESP32";
    struct ble_hs_adv_fields fields = {0};
    fields.flags = BLE_HS_ADV_F_DISC_GEN | BLE_HS_ADV_F_BREDR_UNSUP;
    fields.name = (const uint8_t*)name;
    fields.name_len = sizeof(name) - 1;
    fields.name_is_complete = 1;

    rc = ble_gap_adv_set_fields(&fields);
    if (rc != 0)
    {
        return;
    }

    // 3. Start undirected connectable advertising indefinitely
    struct ble_gap_adv_params adv_params = {0};
    adv_params.conn_mode = BLE_GAP_CONN_MODE_UND;
    adv_params.disc_mode = BLE_GAP_DISC_MODE_GEN;

    ble_gap_adv_start(addr_type, nullptr, BLE_HS_FOREVER, &adv_params, nullptr, nullptr);
}

static void on_reset(int reason)
{
    (void)reason;
    // Host reset; re-synchronization will trigger on_sync again
}

int ble_manager_register_service(const struct ble_gatt_svc_def* svc_def)
{
    struct ble_gatt_svc_def* tmp = realloc(gatt_services_defs, (service_count + 1) * sizeof(*gatt_services_defs));
    if (tmp == NULL)
    {
        return -1;
    }
    gatt_services_defs = tmp;
    gatt_services_defs[service_count] = *svc_def;
    service_count++;
    return 0;
}

int ble_manager_init(void)
{
    // 1. Initialize the NimBLE port (must happen before any NimBLE API calls)
    int rc = nimble_port_init();
    if (rc != 0)
    {
        return rc;
    }

    // 2. Register host callbacks
    ble_hs_cfg.sync_cb = on_sync;
    ble_hs_cfg.reset_cb = on_reset;

    // 3. Initialize GATT service
    ble_svc_gatt_init();

    // 4. Append the terminator sentinel required by ble_gatts_count_cfg
    struct ble_gatt_svc_def* tmp = realloc(gatt_services_defs, (service_count + 1) * sizeof(*gatt_services_defs));
    if (tmp == NULL)
    {
        return -1;
    }
    gatt_services_defs = tmp;
    gatt_services_defs[service_count] = (struct ble_gatt_svc_def){0};

    rc = ble_gatts_count_cfg(gatt_services_defs);
    if (rc != 0)
    {
        return rc;
    }

    rc = ble_gatts_add_svcs(gatt_services_defs);
    if (rc != 0)
    {
        return rc;
    }

    // 5. Start the NimBLE host task
    nimble_port_freertos_init(ble_host_task);

    return 0;
}
