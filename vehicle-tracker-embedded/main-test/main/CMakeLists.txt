idf_component_register(
        SRCS            "main.c" "mbedtls_linux_shims.c" "config/config_test.c" "security/crypto_test.c" "security/security_test.c" "storage/storage_test.c" "uuid/uuid_test.c"
                        "../../components/config/config.c" "../../components/security/security.c" "../../components/security/internal/crypto.c"
        INCLUDE_DIRS    "." "../../components/config" "../../components/security" "../../components/security/internal"
        REQUIRES        "unity" "nvs_flash" "storage" "uuid" "mbedtls"
        WHOLE_ARCHIVE
)
