#ifndef MQTT_H
#define MQTT_H

#include <stdbool.h>

#include "esp_log.h"
#include "mqtt_client.h"

esp_mqtt_client_handle_t mqtt_start(void);
void mqtt_publish_data(esp_mqtt_client_handle_t client, const char *topic, const char *data);

#endif // MQTT_H