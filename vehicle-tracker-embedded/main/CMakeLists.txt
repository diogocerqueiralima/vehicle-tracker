idf_component_register(
        SRCS         "main.c" "ble/ble_manager.c" "ble/security/ble_security.c" "ble/services/gatt_common.c" "ble/services/connection_service.c" "ble/services/gps_service.c" "ble/services/authentication_service.c" "storage/storage.c" "uart/uart.c" "modem/modem.c" "mqtt/mqtt.c"
        INCLUDE_DIRS "."
        REQUIRES "bt" "nvs_flash" "esp_driver_uart" "esp_driver_gpio" "esp_modem" "mqtt"
)
