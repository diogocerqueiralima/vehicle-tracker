#include <stdio.h>
#include "tsim/tsim.h"
#include "esp_log.h"

static const char *TAG = "MAIN";

void app_main() {
    ESP_LOGI(TAG, "Starting application...");
    tsim_init();
    
    while (1) {
        vTaskDelay(pdMS_TO_TICKS(1000));
    }

}