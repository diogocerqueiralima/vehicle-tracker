#include "esp_log.h"
#include "ble/ble_manager.h"
#include "ble/services/configuration_service.h"
#include "driver/gpio.h"
#include "driver/uart.h"
#include "storage/storage.h"
#include "uart/uart.h"

#define LOG_TAG "MAIN"
#define MODEM_PWRKEY_GPIO GPIO_NUM_4

void app_main()
{
    ESP_LOGI(LOG_TAG, "Application started.");

    /*
    // 1. Init storage
    esp_err_t err = init_storage();
    if (err != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize storage: %s", esp_err_to_name(err));
        return;
    }

    // 2. Register BLE services before initializing the manager
    err = ble_manager_register_service(&configuration_service_def);
    if (err != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to register configuration service: %d", err);
        return;
    }

    // 3. Initialize the BLE manager and start the NimBLE host task
    const int error = ble_manager_init();
    if (error != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize BLE manager: %d", error);
        return;
    }
    */

    esp_err_t error = init_uart();

    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize UART: %s", esp_err_to_name(error));
        return;
    }

    ESP_LOGI(LOG_TAG, "UART initialized.");
    error = open_uart(UART_NUM_2, 10, 2048, 115200, 27, 26);
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to open UART: %s", esp_err_to_name(error));
        return;
    }

    ESP_LOGI(LOG_TAG, "UART initialized.");

    const uart_context_t *context = get_uart_context(UART_NUM_2);
    if (context == NULL)
    {
        ESP_LOGE(LOG_TAG, "Failed to get UART context.");
        return;
    }

    ESP_LOGI(LOG_TAG, "UART initialized.");
    const char *data = "AT\r\n";
    error = uart_write(context, data, strlen(data));

    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to write to UART: %s", esp_err_to_name(error));
        return;
    }

    ESP_LOGI(LOG_TAG, "UART initialized.");

    size_t size;
    char *output = uart_read_blocking(context, &size, &error, pdMS_TO_TICKS(1000));

    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to read from UART: %s", esp_err_to_name(error));
        return;
    }

    if (output == nullptr)
    {
        ESP_LOGI(LOG_TAG, "No data received from UART.");
        return;
    }

    printf("%.*s", (int)size, output);

    cleanup_uart();

    while (1)
    {
        vTaskDelay(pdMS_TO_TICKS(1000));
    }
}
