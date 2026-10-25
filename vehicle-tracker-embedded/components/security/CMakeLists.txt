idf_component_register(
        SRCS                "security.c" "internal/crypto.c"
        INCLUDE_DIRS        "."
        PRIV_INCLUDE_DIRS   "internal"
        REQUIRES            "uuid"
        PRIV_REQUIRES       "storage" "mbedtls"
)
