#include <stdio.h>
#include "tsim/tsim.h"
#include "gps/gps.h"
#include "mqtt/mqtt.h"
#include "esp_log.h"

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
        if (result != GPS_SUCCESS) {
            free(data);
            ESP_LOGW("GPS", "%s", gps_get_error_message(result));
            continue;
        }
        tsim_resume_data_mode(modem);

        if (data) {

            char mqtt_payload[256];

            char date_str[7];      // DDMMYY + null
            char utc_str[16];      // hhmmss.sss as string
            snprintf(date_str, sizeof(date_str), "%06lu", data->date);
            snprintf(utc_str, sizeof(utc_str), "%.3f", data->timeUTC);

            snprintf(mqtt_payload, sizeof(mqtt_payload),
                "{"
                    "\"lat\":\"%s\","
                    "\"ns\":\"%c\","
                    "\"lon\":\"%s\","
                    "\"ew\":\"%c\","
                    "\"date\":\"%s\","
                    "\"utc\":\"%s\","
                    "\"alt\":%.2f,"
                    "\"speed\":%.2f,"
                    "\"course\":%.2f"
                "}",
                data->latitude,
                data->latHemisphere,
                data->longitude,
                data->lonHemisphere,
                date_str,
                utc_str,
                data->altitude,
                data->speed,
                data->heading
            );

            esp_mqtt_client_publish(mqtt_client,
                                    "carro/gps",
                                    mqtt_payload,
                                    0,
                                    1,
                                    1);

            free(data);
            ESP_LOGI("GPS", "Published GPS data to MQTT: %s", mqtt_payload);
        } else {
            ESP_LOGW("GPS", "GPS data not ready yet");
        }

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
    
    while (1) {
        vTaskDelay(pdMS_TO_TICKS(1000));
    }

}