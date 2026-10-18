idf_component_register(
        SRCS            "config.c"
        INCLUDE_DIRS    "."
        REQUIRES        "storage" "security" "nvs_flash"
)
