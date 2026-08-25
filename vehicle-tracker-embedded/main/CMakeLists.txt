idf_component_register(
        SRCS        "main.c" "ble/ble_manager.c" "ble/security/ble_security.c" "ble/services/gatt_common.c"
                    "ble/services/connection_service.c" "ble/services/gps_service.c" "ble/services/authentication_service.c"
                    "storage/storage.c" "uart/uart.c" "modem/modem.c" "mqtt/mqtt.c" "i2c/i2c.c" "display/display.c"
                    "identity/device_identity.c"
        INCLUDE_DIRS "."
        REQUIRES "bt" "nvs_flash" "esp_driver_uart" "esp_driver_gpio" "esp_driver_i2c" "esp_modem" "mqtt" "u8g2" "qrcode"
)

target_compile_definitions(${COMPONENT_LIB} PUBLIC U8X8_WITH_USER_PTR)

if(TARGET __idf_u8g2)
    target_compile_definitions(__idf_u8g2 PUBLIC U8X8_WITH_USER_PTR)
endif()
