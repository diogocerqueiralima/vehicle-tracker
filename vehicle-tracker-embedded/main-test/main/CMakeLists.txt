idf_component_register(
        SRCS            "main.c" "storage/storage_test.c"
        INCLUDE_DIRS    "."
        REQUIRES        "unity" "nvs_flash" "storage"
        WHOLE_ARCHIVE
)
