#include "esp_log.h"
#include "at/at.h"
#include "ble/ble_manager.h"
#include "ble/services/configuration_service.h"
#include "driver/gpio.h"
#include "driver/uart.h"
#include "storage/storage.h"
#include "uart/uart.h"

#define LOG_TAG "MAIN"
#define MODEM_PWRKEY_GPIO GPIO_NUM_4

static void modem_power_on(void)
{
    gpio_config_t pwrkey_conf = {
        .pin_bit_mask = (1ULL << MODEM_PWRKEY_GPIO),
        .mode = GPIO_MODE_OUTPUT,
        .pull_up_en = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .intr_type = GPIO_INTR_DISABLE,
    };
    gpio_config(&pwrkey_conf);

    // Example for SIM7600-style modules: pull PWRKEY low for ~500ms-1s, then release.
    gpio_set_level(MODEM_PWRKEY_GPIO, 0);
    vTaskDelay(pdMS_TO_TICKS(1000));
    gpio_set_level(MODEM_PWRKEY_GPIO, 1);

    // Modem needs several seconds to boot before it answers AT commands.
    vTaskDelay(pdMS_TO_TICKS(5000));
}

void app_main()
{
    ESP_LOGI(LOG_TAG, "Application started.");

    //modem_power_on();

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

    ESP_LOGI(LOG_TAG, "UART registry initialized.");
    error = open_uart(UART_NUM_2, 10, 2048, 115200, 27, 26);
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to open UART: %s", esp_err_to_name(error));
        return;
    }

    ESP_LOGI(LOG_TAG, "UART port opened.");

    const uart_context_t *context = get_uart_context(UART_NUM_2);
    if (context == NULL)
    {
        ESP_LOGE(LOG_TAG, "Failed to get UART context.");
        return;
    }

    error = disable_echo(context);
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to disable echo: %s", esp_err_to_name(error));
        return;
    }

    ESP_LOGI(LOG_TAG, "UART context acquired.");
    const char *data = "AT\r\n";
    ESP_LOGI(LOG_TAG, "Sending AT command...");

    size_t response_size;
    char *response = send_at_command_with_response(context, data, strlen(data), "OK", &response_size, &error);

    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to send AT command: %s", esp_err_to_name(error));
        return;
    }

    printf("Received response (%zu bytes): '%.*s'\n", response_size, (int)response_size, response);
    free(response);

    cleanup_uart();

    while (1)
    {
        vTaskDelay(pdMS_TO_TICKS(1000));
    }
}
