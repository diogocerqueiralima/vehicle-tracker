idf_component_register(
        SRCS         "main.c" "ble/ble_manager.c" "ble/services/configuration_service.c" "storage/storage.c" "uart/uart.c"
        INCLUDE_DIRS "."
        REQUIRES "bt" "nvs_flash" "esp_driver_uart" "esp_driver_gpio"
)