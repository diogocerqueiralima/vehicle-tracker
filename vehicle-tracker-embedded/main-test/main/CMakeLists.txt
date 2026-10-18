idf_component_register(
        SRCS            "main.c" "storage/storage_test.c" "uuid/uuid_test.c"
        INCLUDE_DIRS    "."
        REQUIRES        "unity" "nvs_flash" "storage" "uuid"
        WHOLE_ARCHIVE
)
