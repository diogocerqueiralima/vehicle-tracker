idf_component_register(SRCS "main.c" "tsim/tsim.c" "utils/at.c" "mqtt/mqtt.c" "secrets/secrets.c" "gps/gps.c"
                    INCLUDE_DIRS "."
                    REQUIRES esp_driver_gpio esp_netif esp_event esp_modem)