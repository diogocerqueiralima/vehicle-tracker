idf_component_register(
        SRCS            "security.c"
        INCLUDE_DIRS    "."
        REQUIRES        "storage" "uuid" "mbedtls"
)
