idf_component_register(
        SRCS            "config.c"
        INCLUDE_DIRS    "."
        PRIV_REQUIRES   "security" "storage" "validation" "nvs_flash"
)
