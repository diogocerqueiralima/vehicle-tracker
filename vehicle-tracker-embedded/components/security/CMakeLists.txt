idf_component_register(
        SRCS            "security.c"
        INCLUDE_DIRS    "."
        REQUIRES        "uuid"
        PRIV_REQUIRES   "storage"
)
