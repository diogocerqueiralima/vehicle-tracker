#include "at.h"

static const char *TAG = "AT_UTILS";

esp_err_t print_line_callback(uint8_t *data, size_t len) {
    ESP_LOGI(TAG, "Modem response: %.*s", len, data);
    return ESP_OK;
}