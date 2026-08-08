#include "gps_service.h"

#include <string.h>
#include "gatt_common.h"

// Validates that the GPS update interval is exactly 4 bytes (uint32_t) and greater than zero.
static bool validate_gps_update_interval(const char* data, const uint16_t len)
{
    if (len != sizeof(uint32_t))
    {
        return false;
    }

    uint32_t value;
    memcpy(&value, data, sizeof(uint32_t));
    return value > 0;
}

// Validates that the GPS timeout is exactly 4 bytes (uint32_t) and greater than zero.
static bool validate_gps_timeout(const char* data, const uint16_t len)
{
    if (len != sizeof(uint32_t))
    {
        return false;
    }

    uint32_t value;
    memcpy(&value, data, sizeof(uint32_t));
    return value > 0;
}

// Validates that the GPS mode is one of "standalone", "ue-based", or "ue-assisted".
static bool validate_gps_mode(const char* data, const uint16_t len)
{
    static const char* STANDALONE = "standalone";
    static const char* UE_BASED = "ue-based";
    static const char* UE_ASSISTED = "ue-assisted";

    return (len == strlen(STANDALONE) && strncmp(data, STANDALONE, len) == 0) ||
           (len == strlen(UE_BASED) && strncmp(data, UE_BASED, len) == 0) ||
           (len == strlen(UE_ASSISTED) && strncmp(data, UE_ASSISTED, len) == 0);
}

static const ble_uuid128_t gps_service_uuid =
    BLE_UUID128_INIT(0x2d, 0xd1, 0x29, 0xbf, 0xa9, 0x34, 0x48, 0x71, 0x98, 0x13, 0x79, 0xca, 0x3d, 0x4c, 0xd5, 0x6e);

static const ble_uuid128_t gps_update_interval_uuid =
    BLE_UUID128_INIT(0x26, 0x91, 0xf2, 0x17, 0x59, 0x21, 0x0b, 0x93, 0x13, 0x8f, 0x23, 0xa2, 0x18, 0x61, 0x58, 0xa7);

static const ble_uuid128_t gps_timeout_uuid =
    BLE_UUID128_INIT(0xd1, 0x83, 0x2c, 0xf4, 0xf6, 0xda, 0x0f, 0x19, 0x13, 0x42, 0xf5, 0x1e, 0x47, 0x16, 0x56, 0xca);

static const ble_uuid128_t gps_mode_uuid =
    BLE_UUID128_INIT(0xc6, 0x56, 0xf3, 0x59, 0xed, 0xe5, 0x44, 0x25, 0x8c, 0xc6, 0xdb, 0xa8, 0xfc, 0xd5, 0x7e, 0xd6);

static const struct ble_gatt_chr_def characteristics[] = {
    {
        .uuid = &gps_update_interval_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN |
                 BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_WRITE_AUTHEN,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = GPS_UPDATE_INTERVAL_NAMESPACE,
            .name = "GPS update interval",
            .validate = validate_gps_update_interval,
        }
    },
    {
        .uuid = &gps_timeout_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN |
                 BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_WRITE_AUTHEN,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = GPS_TIMEOUT_NAMESPACE,
            .name = "GPS timeout",
            .validate = validate_gps_timeout,
        }
    },
    {
        .uuid = &gps_mode_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_READ_AUTHEN |
                 BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_WRITE_AUTHEN,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = GPS_MODE_NAMESPACE,
            .name = "GPS mode",
            .validate = validate_gps_mode,
        }
    },
    {0},
};

const struct ble_gatt_svc_def gps_service_def = {
    .type = BLE_GATT_SVC_TYPE_PRIMARY,
    .uuid = &gps_service_uuid.u,
    .characteristics = characteristics,
};
