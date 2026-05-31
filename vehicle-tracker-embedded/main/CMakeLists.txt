idf_component_register(
        SRCS         "main.c" "ble/ble_manager.c" "ble/services/configuration_service.c"
        INCLUDE_DIRS "."
        REQUIRES "bt"
)