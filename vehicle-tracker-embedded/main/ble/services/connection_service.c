#include "connection_service.h"

#include <string.h>
#include "gatt_common.h"

// Validates that the MQTT broker URL starts with "mqtt://" or "mqtts://" and does not exceed 256 bytes.
static bool validate_mqtt_broker(const char* data, const uint16_t len)
{
    if (len == 0 || len > 256)
    {
        return false;
    }

    const bool is_mqtt = len > 7 && strncmp(data, "mqtt://", 7) == 0;
    const bool is_mqtts = len > 8 && strncmp(data, "mqtts://", 8) == 0;

    return is_mqtt || is_mqtts;
}

// Validates that the MQTT keep-alive interval is exactly 2 bytes (uint16_t).
static bool validate_mqtt_keep_alive(const char* data, const uint16_t len)
{
    (void)data;
    return len == sizeof(uint16_t);
}

// Validates that the MQTT QOS level is a single byte with value 0, 1, or 2.
static bool validate_mqtt_qos(const char* data, const uint16_t len)
{
    if (len != sizeof(uint8_t))
    {
        return false;
    }

    return (uint8_t)*data <= 2;
}

// Validates that the MQTT reconnection interval is exactly 4 bytes (uint32_t) and greater than zero.
static bool validate_mqtt_reconnection_interval(const char* data, const uint16_t len)
{
    if (len != sizeof(uint32_t))
    {
        return false;
    }

    uint32_t value;
    memcpy(&value, data, sizeof(uint32_t));
    return value > 0;
}

static const ble_uuid128_t connection_service_uuid =
    BLE_UUID128_INIT(0x65, 0x78, 0x87, 0x8c, 0x85, 0xc9, 0x05, 0x66, 0x1a, 0x51, 0x37, 0xc9, 0xaa, 0xea, 0x04, 0x13);

static const ble_uuid128_t connection_mqtt_broker_uuid =
    BLE_UUID128_INIT(0x00, 0x2a, 0xf4, 0x40, 0x71, 0x57, 0x05, 0xf5, 0x29, 0x6f, 0x0c, 0x87, 0xfa, 0x0e, 0x5e, 0x75);

static const ble_uuid128_t connection_mqtt_keep_alive_uuid =
    BLE_UUID128_INIT(0x19, 0x3b, 0xdb, 0xf3, 0x3f, 0x36, 0x0b, 0xd1, 0x20, 0x6f, 0xe8, 0xbc, 0x31, 0xf1, 0x97, 0x8f);

static const ble_uuid128_t connection_mqtt_qos_uuid =
    BLE_UUID128_INIT(0x49, 0x73, 0x78, 0xba, 0x02, 0x52, 0x0d, 0x84, 0x20, 0x7f, 0x05, 0xa8, 0x86, 0xd0, 0x11, 0x86);

static const ble_uuid128_t connection_mqtt_reconnection_interval_uuid =
    BLE_UUID128_INIT(0x14, 0x72, 0xf2, 0x95, 0xa2, 0x0b, 0x0c, 0x4b, 0x31, 0x91, 0x7f, 0x67, 0xb0, 0xec, 0xaa, 0x7c);

static const struct ble_gatt_chr_def characteristics[] = {
    {
        .uuid = &connection_mqtt_broker_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_READ_ENC | BLE_GATT_CHR_F_WRITE_ENC,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = MQTT_BROKER_NAMESPACE,
            .name = "MQTT broker",
            .validate = validate_mqtt_broker,
        },
    },
    {
        .uuid = &connection_mqtt_keep_alive_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_READ_ENC | BLE_GATT_CHR_F_WRITE_ENC,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = MQTT_KEEP_ALIVE_INTERVAL_NAMESPACE,
            .name = "MQTT keep alive interval",
            .validate = validate_mqtt_keep_alive,
        },
    },
    {
        .uuid = &connection_mqtt_qos_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_READ_ENC | BLE_GATT_CHR_F_WRITE_ENC,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = MQTT_QOS_NAMESPACE,
            .name = "MQTT QOS",
            .validate = validate_mqtt_qos,
        },
    },
    {
        .uuid = &connection_mqtt_reconnection_interval_uuid.u,
        .access_cb = gatt_common_access_cb,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE | BLE_GATT_CHR_F_READ_ENC | BLE_GATT_CHR_F_WRITE_ENC,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = MQTT_RECONNECTION_INTERVAL_NAMESPACE,
            .name = "MQTT reconnection interval",
            .validate = validate_mqtt_reconnection_interval,
        }
    },
    {0},
};

const struct ble_gatt_svc_def connection_service_def = {
    .type = BLE_GATT_SVC_TYPE_PRIMARY,
    .uuid = &connection_service_uuid.u,
    .characteristics = characteristics,
};
