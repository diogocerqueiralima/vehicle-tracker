#include "mqtt.h"
#include "../secrets/secrets.h"

esp_mqtt_client_handle_t mqtt_start(void) {
    
    esp_mqtt_client_config_t mqtt_cfg = {
        .broker = {
            .address = {
                .hostname = MQTT_HOSTNAME,
                .port = MQTT_PORT,
                .transport = MQTT_TRANSPORT_OVER_SSL
            },
            .verification = {
                .certificate = MQTT_CA_CERT
            }
        },
        .credentials = {
            .username = MQTT_USERNAME,
            .client_id = MQTT_CLIENT_ID,
            .authentication = {
                .password = MQTT_PASSWORD
            }
        },
        .session = {
            .keepalive = 30,
            .disable_keepalive = false
        }
    };

    esp_mqtt_client_handle_t client = esp_mqtt_client_init(&mqtt_cfg);
    esp_err_t err = esp_mqtt_client_start(client);

    if (err == ESP_OK) {
        ESP_LOGI("MQTT", "MQTT client started successfully");
        return client;
    }
    
    ESP_LOGE("MQTT", "Failed to start MQTT client: %s", esp_err_to_name(err));
    return NULL;
}

void mqtt_publish_data(esp_mqtt_client_handle_t client, const char *topic, const char *data) {

    int msg_id = esp_mqtt_client_publish(client, topic, data, 0, 1, 1);
    if (msg_id != -1) {
        ESP_LOGI("MQTT", "Published data to topic %s, msg_id=%d", topic, msg_id);
    } else {
        ESP_LOGE("MQTT", "Failed to publish data to topic %s", topic);
    }

}