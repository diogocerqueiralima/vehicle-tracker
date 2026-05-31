#include "esp_log.h"
#include "ble/ble_manager.h"
#include "ble/services/configuration_service.h"

#define LOG_TAG "MAIN"

void app_main() {

    ESP_LOGI(LOG_TAG, "Application started.");

    // 1. Register BLE services before initializing the manager
    ble_manager_register_service(&configuration_service_def);

    // 2. Initialize the BLE manager and start the NimBLE host task
    const int error = ble_manager_init();
    if (error != 0) {
        ESP_LOGE(LOG_TAG, "Failed to initialize BLE manager: %d", error);
        return;
    }

    while (1) {
        vTaskDelay(1000);
    }
}
