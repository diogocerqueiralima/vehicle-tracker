#include "esp_log.h"
#include "ble/ble_manager.h"
#include "ble/services/configuration_service.h"
#include "storage/storage.h"

#define LOG_TAG "MAIN"

void app_main() {

    ESP_LOGI(LOG_TAG, "Application started.");

    // 1. Init storage
    esp_err_t err = init_storage();
    if (err != ESP_OK) {
        ESP_LOGE(LOG_TAG, "Failed to initialize storage: %s", esp_err_to_name(err));
        return;
    }

    // 2. Register BLE services before initializing the manager
    err = ble_manager_register_service(&configuration_service_def);
    if (err != 0) {
        ESP_LOGE(LOG_TAG, "Failed to register configuration service: %d", err);
        return;
    }

    // 3. Initialize the BLE manager and start the NimBLE host task
    const int error = ble_manager_init();
    if (error != 0) {
        ESP_LOGE(LOG_TAG, "Failed to initialize BLE manager: %d", error);
        return;
    }

    while (1) {
        vTaskDelay(pdMS_TO_TICKS(1000));
    }

}
