#include "ble_manager.h"

#include <inttypes.h>
#include <stdlib.h>
#include "esp_log.h"
#include "host/ble_store.h"
#include "identity/device_identity.h"
#include "nimble/nimble_port.h"
#include "nimble/nimble_port_freertos.h"
#include "security/ble_security.h"

static const char* LOG_TAG = "ble_manager";
static const char* DEVICE_NAME = "ESP32\0";

// Reserved "no assigned Company ID" placeholder (BT SIG), used since we don't have a registered
// Company ID yet. Manufacturer-specific AD data must be prefixed with a 2-byte Company ID (little-endian).
#define BLE_UNASSIGNED_COMPANY_ID 0xFFFF

static struct ble_gatt_svc_def* gatt_services_defs = nullptr;
static int service_count = 0;

static ble_manager_gap_cb_t app_gap_cb = nullptr;
static void* app_gap_cb_arg = nullptr;

static void ble_host_task(void* param)
{
    (void)param;
    nimble_port_run();
    nimble_port_freertos_deinit();
}

static int on_gap_event(struct ble_gap_event* event, void* arg);

// Starts undirected connectable advertising indefinitely, using the device's inferred address type.
static void start_advertising()
{
    // 1. Infer own address type (public if available, random static otherwise)
    uint8_t addr_type;
    int rc = ble_hs_id_infer_auto(0, &addr_type);
    if (rc != 0)
    {
        return;
    }

    // 2. Build advertising payload: general-discoverable, complete local name, and device ID in manufacturer-specific data
    struct ble_hs_adv_fields fields = {0};

    // 2.1 Set flags to indicate general discoverable mode and BR/EDR not supported
    fields.flags = BLE_HS_ADV_F_DISC_GEN | BLE_HS_ADV_F_BREDR_UNSUP;

    // 2.2 Set the complete local name to the device name
    fields.name = (const uint8_t*)DEVICE_NAME;
    fields.name_len = strlen(DEVICE_NAME);
    fields.name_is_complete = 1;

    // 2.3 Add device ID to advertising payload
    uint8_t mfg_data[2 + DEVICE_IDENTITY_ID_LEN];
    mfg_data[0] = (uint8_t)(BLE_UNASSIGNED_COMPANY_ID & 0xFF);
    mfg_data[1] = (uint8_t)(BLE_UNASSIGNED_COMPANY_ID >> 8);

    const esp_err_t error = device_identity_get(mfg_data + 2);
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to load device identity for advertising: %s", esp_err_to_name(error));
    }
    else {
        fields.mfg_data = mfg_data;
        fields.mfg_data_len = sizeof(mfg_data);
    }

    // 2.4 Set the advertising fields
    rc = ble_gap_adv_set_fields(&fields);
    if (rc != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to set advertising fields: %d", rc);
        return;
    }

    // 3. Start undirected connectable advertising indefinitely
    struct ble_gap_adv_params adv_params = {0};
    adv_params.conn_mode = BLE_GAP_CONN_MODE_UND;
    adv_params.disc_mode = BLE_GAP_DISC_MODE_GEN;

    ble_gap_adv_start(addr_type, nullptr, BLE_HS_FOREVER, &adv_params, on_gap_event, nullptr);
}

// Handles security-related GAP events: passkey display, encryption result, repeat pairing, and
// disconnection (advertising stops automatically on connect and must be restarted).
static int on_gap_event(struct ble_gap_event* event, void* arg)
{
    (void)arg;

    switch (event->type)
    {
    case BLE_GAP_EVENT_DISCONNECT:
        {
            ESP_LOGI(LOG_TAG, "Disconnected (reason %d), restarting advertising", event->disconnect.reason);
            if (app_gap_cb != nullptr)
            {
                app_gap_cb(BLE_MANAGER_EVENT_DISCONNECTED, nullptr, app_gap_cb_arg);
            }
            start_advertising();
            return 0;
        }
    case BLE_GAP_EVENT_PASSKEY_ACTION:
        {
            if (event->passkey.params.action == BLE_SM_IOACT_DISP)
            {
                struct ble_sm_io pkey = {0};
                pkey.action = event->passkey.params.action;
                pkey.passkey = generate_passkey();

                if (app_gap_cb != nullptr)
                {
                    app_gap_cb(BLE_MANAGER_EVENT_PASSKEY, &pkey.passkey, app_gap_cb_arg);
                }

                ble_sm_inject_io(event->passkey.conn_handle, &pkey);
            }
            return 0;
        }
    case BLE_GAP_EVENT_ENC_CHANGE:
        {
            if (event->enc_change.status == 0)
            {
                struct ble_gap_conn_desc desc;
                const int rc = ble_gap_conn_find(event->enc_change.conn_handle, &desc);
                if (rc == 0 && desc.sec_state.bonded)
                {
                    ESP_LOGI(LOG_TAG, "Connected (connection %d, bonded)", event->enc_change.conn_handle);
                    if (app_gap_cb != nullptr)
                    {
                        app_gap_cb(BLE_MANAGER_EVENT_CONNECTED, nullptr, app_gap_cb_arg);
                    }
                }
                else
                {
                    ESP_LOGI(LOG_TAG, "Connection %d encrypted", event->enc_change.conn_handle);
                }
            }
            else
            {
                ESP_LOGE(
                    LOG_TAG, "Encryption failed for connection %d: %d", event->enc_change.conn_handle,
                    event->enc_change.status
                );
            }
            return 0;
        }
    case BLE_GAP_EVENT_REPEAT_PAIRING:
        {
            struct ble_gap_conn_desc desc;
            const int rc = ble_gap_conn_find(event->repeat_pairing.conn_handle, &desc);
            if (rc == 0)
            {
                ble_store_util_delete_peer(&desc.peer_id_addr);
            }
            return BLE_GAP_REPEAT_PAIRING_RETRY;
        }
    default:
        return 0;
    }
}

static void on_sync()
{
    start_advertising();
}

static void on_reset(int reason)
{
    (void)reason;
    // Host reset; re-synchronization will trigger on_sync again
}

void ble_manager_set_gap_callback(ble_manager_gap_cb_t cb, void* arg)
{
    app_gap_cb = cb;
    app_gap_cb_arg = arg;
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

int ble_manager_init()
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

    // 3. Configure the Security Manager (bonding, MITM protection, passkey pairing)
    ble_security_init();

    // 4. Initialize GATT service
    ble_svc_gatt_init();

    // 5. Append the terminator sentinel required by ble_gatts_count_cfg
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

    // 6. Start the NimBLE host task
    nimble_port_freertos_init(ble_host_task);

    return 0;
}
