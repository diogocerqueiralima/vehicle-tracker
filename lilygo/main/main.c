#include <stdio.h>
#include "tsim/tsim.h"
#include "gps/gps.h"
#include "mqtt/mqtt.h"
#include "utils/encoding/gps_encode.h"
#include "esp_log.h"

#define BOARD_LED   12

static const char *TAG = "MAIN";

esp_modem_dce_t *modem = NULL;

void gps_task(void *arg) {

    esp_mqtt_client_handle_t mqtt_client = (esp_mqtt_client_handle_t)arg;

    while (1) {
        
        gps_data_t *data = malloc(sizeof(gps_data_t));
        if (data == NULL) {
            ESP_LOGE("GPS", "Failed to allocate memory for GPS data.");
            vTaskDelay(pdMS_TO_TICKS(1000));
            continue;
        }

        tsim_pause_data_mode(modem);
        int result = gps_get_data(data, modem);
        tsim_resume_data_mode(modem);

        if (result != GPS_SUCCESS) {

            free(data);
            ESP_LOGW("GPS", "%s", gps_get_error_message(result));

            gpio_set_level(BOARD_LED, 0); // Turn off LED to indicate no GPS fix
            vTaskDelay(pdMS_TO_TICKS(500));
            gpio_set_level(BOARD_LED, 1); // Turn on LED

            continue;
        }

        size_t bytes;
        uint8_t *mqtt_payload = gps_encode_location(data, &bytes);

        if (mqtt_payload != NULL) {
            esp_mqtt_client_publish(mqtt_client, "carro/gps", (const char *) mqtt_payload, bytes, 0, 0);
            free(mqtt_payload);
        } else {
            ESP_LOGE("GPS", "Failed to encode GPS data.");
        }

        free(data);
        vTaskDelay(pdMS_TO_TICKS(1000));
    }

}

static void ppp_event_handler(void *arg, esp_event_base_t event_base, int32_t event_id, void *event_data) {

    ESP_LOGI(TAG, "PPP event received: base=%s, id=%d", event_base, event_id);

    if (event_id == IP_EVENT_PPP_GOT_IP) {

        ip_event_got_ip_t *event = (ip_event_got_ip_t *) event_data;


        ESP_LOGI(TAG, "PPP got IP address.");
        ESP_LOGI(TAG, "IP: " IPSTR, IP2STR(&event->ip_info.ip));

        esp_mqtt_client_handle_t mqtt_client = mqtt_start();

        if (mqtt_client == NULL) {
            ESP_LOGE(TAG, "Failed to start MQTT client.");
            return;
        }

        xTaskCreate(gps_task, "gps_task", 4096, (void *)mqtt_client, 5, NULL);
    }

}
void app_main() {
    
    ESP_LOGI(TAG, "Starting application...");
    
    int result = tsim_init(&modem, ppp_event_handler);
    if (result != TSIM_SUCCESS) {
        ESP_LOGE(TAG, "%s", tsim_get_error_message(result));
        return;
    }

    result = gps_init(modem);
    if (result != GPS_SUCCESS) {
        ESP_LOGE(TAG, "%s", gps_get_error_message(result));
        return;
    }

    gps_status_t *status = malloc(sizeof(gps_status_t));

    if (status == NULL) {
        ESP_LOGE(TAG, "Failed to allocate memory for GPS status.");
        return;
    }

    result = gps_get_mode(status, modem);
    if (result != GPS_SUCCESS) {
        ESP_LOGE(TAG, "%s", gps_get_error_message(result));
        free(status);
        return;
    }

    ESP_LOGI(TAG, "GPS Power: %d, Mode: %d", status->power, status->mode);
    if (status->power == GPS_OFF) {

        ESP_LOGI(TAG, "Enabling GPS...");

        result = gps_enable(modem);
        if (result != GPS_SUCCESS) {
            ESP_LOGE(TAG, "%s", gps_get_error_message(result));
            free(status);
            return;
        }

        result = gps_get_mode(status, modem);
        if (result != GPS_SUCCESS) {
            ESP_LOGE(TAG, "%s", gps_get_error_message(result));
            free(status);
            return;
        }

        ESP_LOGI(TAG, "GPS enabled. Power: %d, Mode: %d", status->power, status->mode);
    }

    free(status);

    result = tsim_set_data_mode(modem);
    if (result != TSIM_SUCCESS) {
        ESP_LOGE(TAG, "%s", tsim_get_error_message(result));
        return;
    }

    gpio_reset_pin(BOARD_LED);
    gpio_set_direction(BOARD_LED, GPIO_MODE_OUTPUT);
    gpio_set_level(BOARD_LED, 1); // Turn on LED to indicate system is running
    
    while (1) {
        vTaskDelay(pdMS_TO_TICKS(1000));
    }

}
