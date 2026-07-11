#include "configuration_service.h"

#include <string.h>
#include "esp_log.h"
#include "storage/storage.h"

static const char* LOG_TAG = "configuration_service";

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

// Validates that the MQTT topic is non-empty, does not exceed 256 bytes, and contains no wildcard characters.
static bool validate_mqtt_topic(const char* data, const uint16_t len)
{
    if (len == 0 || len > 256)
    {
        return false;
    }

    for (uint16_t i = 0; i < len; i++)
    {
        if (data[i] == '#' || data[i] == '+')
        {
            return false;
        }
    }

    return true;
}

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

// Validates that the CSR is a PEM-encoded certificate signing request.
static bool validate_csr(const char* data, const uint16_t len)
{
    static const char* PEM_HEADER = "-----BEGIN CERTIFICATE REQUEST-----";
    const size_t header_len = strlen(PEM_HEADER);
    return len >= header_len && strncmp(data, PEM_HEADER, header_len) == 0;
}

// Validates that the certificate is a PEM-encoded X.509 certificate.
static bool validate_certificate(const char* data, const uint16_t len)
{
    static const char* PEM_HEADER = "-----BEGIN CERTIFICATE-----";
    const size_t header_len = strlen(PEM_HEADER);
    return len >= header_len && strncmp(data, PEM_HEADER, header_len) == 0;
}

static const ble_uuid128_t configuration_service_uuid =
    BLE_UUID128_INIT(0x65, 0x78, 0x87, 0x8c, 0x85, 0xc9, 0x05, 0x66, 0x1a, 0x51, 0x37, 0xc9, 0xaa, 0xea, 0x04, 0x13);

static const ble_uuid128_t configuration_mqtt_broker_uuid =
    BLE_UUID128_INIT(0x00, 0x2a, 0xf4, 0x40, 0x71, 0x57, 0x05, 0xf5, 0x29, 0x6f, 0x0c, 0x87, 0xfa, 0x0e, 0x5e, 0x75);

static const ble_uuid128_t configuration_mqtt_keep_alive_uuid =
    BLE_UUID128_INIT(0x19, 0x3b, 0xdb, 0xf3, 0x3f, 0x36, 0x0b, 0xd1, 0x20, 0x6f, 0xe8, 0xbc, 0x31, 0xf1, 0x97, 0x8f);

static const ble_uuid128_t configuration_mqtt_qos_uuid =
    BLE_UUID128_INIT(0x49, 0x73, 0x78, 0xba, 0x02, 0x52, 0x0d, 0x84, 0x20, 0x7f, 0x05, 0xa8, 0x86, 0xd0, 0x11, 0x86);

static const ble_uuid128_t configuration_mqtt_reconnection_interval_uuid =
    BLE_UUID128_INIT(0x14, 0x72, 0xf2, 0x95, 0xa2, 0x0b, 0x0c, 0x4b, 0x31, 0x91, 0x7f, 0x67, 0xb0, 0xec, 0xaa, 0x7c);

static const ble_uuid128_t configuration_mqtt_topic_uuid =
    BLE_UUID128_INIT(0x9e, 0x0a, 0xb4, 0x2d, 0xc0, 0xc9, 0x02, 0x35, 0x33, 0xa1, 0x79, 0x05, 0x4c, 0xf9, 0x2b, 0xc5);

static const ble_uuid128_t configuration_gps_update_interval_uuid =
    BLE_UUID128_INIT(0x26, 0x91, 0xf2, 0x17, 0x59, 0x21, 0x0b, 0x93, 0x13, 0x8f, 0x23, 0xa2, 0x18, 0x61, 0x58, 0xa7);

static const ble_uuid128_t configuration_gps_timeout_uuid =
    BLE_UUID128_INIT(0xd1, 0x83, 0x2c, 0xf4, 0xf6, 0xda, 0x0f, 0x19, 0x13, 0x42, 0xf5, 0x1e, 0x47, 0x16, 0x56, 0xca);

static const ble_uuid128_t configuration_csr_uuid =
    BLE_UUID128_INIT(0xa1, 0xb2, 0xc3, 0xd4, 0xe5, 0xf6, 0x08, 0x90, 0x2b, 0xcd, 0xef, 0x12, 0x34, 0x56, 0x78, 0x90);

static const ble_uuid128_t configuration_certificate_uuid =
    BLE_UUID128_INIT(0x61, 0x55, 0x51, 0xac, 0x42, 0xa4, 0x08, 0xe8, 0x36, 0xd8, 0x7b, 0x10, 0x8d, 0x26, 0x5c, 0xdf);

static int configure_gatt_characteristic(
    uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt* ctxt, void* arg
)
{
    (void)conn_handle;
    (void)attr_handle;

    const gatt_handler_context_t* ctx = (gatt_handler_context_t*)arg;

    switch (ctxt->op)
    {
    case BLE_GATT_ACCESS_OP_READ_CHR:
        {
            ESP_LOGI(LOG_TAG, "Reading %s configuration", ctx->name);

            // 1. Get the size of the stored value to determine how much data to read.
            size_t len = 0;
            esp_err_t err = get_data_size(ctx->namespace, &len);
            if (err != ESP_OK)
            {
                ESP_LOGE(LOG_TAG, "Failed to get %s size: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 2. Handle the case where no value is stored yet (len == 0) by returning an error to indicate that the value is not available.
            if (len == 0)
            {
                ESP_LOGE(LOG_TAG, "Stored %s value is empty", ctx->name);
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 3. Read the stored value.
            char buf[len];
            err = load_data(ctx->namespace, buf, len);
            if (err != ESP_OK)
            {
                ESP_LOGE(LOG_TAG, "Failed to load %s: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            // 4. Append the value to the response buffer to be sent back to the client.
            if (os_mbuf_append(ctxt->om, buf, len) != 0)
            {
                return BLE_ATT_ERR_INSUFFICIENT_RES;
            }

            ESP_LOGI(LOG_TAG, "Successfully read %s", ctx->name);
            break;
        }
    case BLE_GATT_ACCESS_OP_WRITE_CHR:
        {
            ESP_LOGI(LOG_TAG, "Writing %s configuration", ctx->name);

            // 5. Check if the length of the incoming data is valid
            const uint16_t len = OS_MBUF_PKTLEN(ctxt->om);
            if (len <= 0)
            {
                return BLE_ATT_ERR_INVALID_ATTR_VALUE_LEN;
            }

            // 6. Copy the incoming data from the mbuf into a local buffer for validation and storage.
            char buf[len];
            os_mbuf_copydata(ctxt->om, 0, len, buf);

            // 7. Validate the value using the provided validation function, if any.
            if (ctx->validate != NULL && !ctx->validate(buf, len))
            {
                ESP_LOGE(LOG_TAG, "Invalid value for %s:", ctx->name);
                ESP_LOG_BUFFER_HEX_LEVEL(LOG_TAG, buf, len, ESP_LOG_ERROR);
                return BLE_ATT_ERR_VALUE_NOT_ALLOWED;
            }

            // 8. Save the value to NVS for persistent storage.
            const esp_err_t err = save_data(ctx->namespace, buf, len);
            if (err != ESP_OK)
            {
                ESP_LOGE(LOG_TAG, "Failed to save %s: %s", ctx->name, esp_err_to_name(err));
                return BLE_ATT_ERR_UNLIKELY;
            }

            ESP_LOGI(LOG_TAG, "Successfully saved %s", ctx->name);
            break;
        }
    default:
        break;
    }

    return 0;
}

static const struct ble_gatt_chr_def characteristics[] = {
    {
        .uuid = &configuration_mqtt_broker_uuid.u,
        .access_cb = configure_gatt_characteristic,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = MQTT_BROKER_NAMESPACE,
            .name = "MQTT broker",
            .validate = validate_mqtt_broker,
        },
    },
    {
        .uuid = &configuration_mqtt_keep_alive_uuid.u,
        .access_cb = configure_gatt_characteristic,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = MQTT_KEEP_ALIVE_INTERVAL_NAMESPACE,
            .name = "MQTT keep alive interval",
            .validate = validate_mqtt_keep_alive,
        },
    },
    {
        .uuid = &configuration_mqtt_qos_uuid.u,
        .access_cb = configure_gatt_characteristic,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = MQTT_QOS_NAMESPACE,
            .name = "MQTT QOS",
            .validate = validate_mqtt_qos,
        },
    },
    {
        .uuid = &configuration_mqtt_reconnection_interval_uuid.u,
        .access_cb = configure_gatt_characteristic,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = MQTT_RECONNECTION_INTERVAL_NAMESPACE,
            .name = "MQTT reconnection interval",
            .validate = validate_mqtt_reconnection_interval,
        }
    },
    {
        .uuid = &configuration_mqtt_topic_uuid.u,
        .access_cb = configure_gatt_characteristic,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = MQTT_TOPIC_NAMESPACE,
            .name = "MQTT topic",
            .validate = validate_mqtt_topic,
        }
    },
    {
        .uuid = &configuration_gps_update_interval_uuid.u,
        .access_cb = configure_gatt_characteristic,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = GPS_UPDATE_INTERVAL_NAMESPACE,
            .name = "GPS update interval",
            .validate = validate_gps_update_interval,
        }
    },
    {
        .uuid = &configuration_gps_timeout_uuid.u,
        .access_cb = configure_gatt_characteristic,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = GPS_TIMEOUT_NAMESPACE,
            .name = "GPS timeout",
            .validate = validate_gps_timeout,
        }
    },
    {
        .uuid = &configuration_csr_uuid.u,
        .access_cb = configure_gatt_characteristic,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = CSR_NAMESPACE,
            .name = "CSR",
            .validate = validate_csr,
        }
    },
    {
        .uuid = &configuration_certificate_uuid.u,
        .access_cb = configure_gatt_characteristic,
        .flags = BLE_GATT_CHR_F_READ | BLE_GATT_CHR_F_WRITE,
        .val_handle = nullptr,
        .arg = &(gatt_handler_context_t){
            .namespace = CERTIFICATE_NAMESPACE,
            .name = "Certificate",
            .validate = validate_certificate,
        }
    },
    {0},
};

const struct ble_gatt_svc_def configuration_service_def = {
    .type = BLE_GATT_SVC_TYPE_PRIMARY,
    .uuid = &configuration_service_uuid.u,
    .characteristics = characteristics,
};
