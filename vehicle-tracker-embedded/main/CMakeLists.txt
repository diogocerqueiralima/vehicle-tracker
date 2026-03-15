idf_component_register(
    SRCS            "main.c" "tsim/tsim.c" "utils/at.c" "mqtt/mqtt.c" "secrets/secrets.c" 
                    "gps/gps.c" "utils/encoding/gps_encode.c" "generated/vehicle-tracker-schemas/Location.pb.c"
                    "generated/vehicle-tracker-schemas/Telemetry.pb.c"
    INCLUDE_DIRS    "."
                    "utils/encoding"
                    "generated"
    REQUIRES        esp_driver_gpio esp_netif esp_event esp_modem
)