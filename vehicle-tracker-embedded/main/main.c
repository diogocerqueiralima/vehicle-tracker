#include "esp_event.h"
#include "esp_log.h"
#include "esp_netif.h"
#include "modem/modem.h"

#define LOG_TAG "MAIN"

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
    err = ble_manager_register_service(&connection_service_def);
    if (err != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to register connection service: %d", err);
        return;
    }

    err = ble_manager_register_service(&gps_service_def);
    if (err != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to register GPS service: %d", err);
        return;
    }

    err = ble_manager_register_service(&authentication_service_def);
    if (err != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to register authentication service: %d", err);
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

    // 1. Initialize the network interface
    esp_err_t error = esp_netif_init();
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize network interface: %s", esp_err_to_name(error));
        return;
    }

    // 2. Create the default event loop
    error = esp_event_loop_create_default();
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to create default loop event");
        return;
    }

    // 3. Create a new network interface for the PPP connection
    const esp_netif_config_t esp_netif_config = ESP_NETIF_DEFAULT_PPP();
    esp_netif_t *netif = esp_netif_new(&esp_netif_config);
    if (netif == NULL)
    {
        ESP_LOGE(LOG_TAG, "No network interface found");
        return;
    }

    // 4. Set the default network interface to the newly created PPP interface
    error = esp_netif_set_default_netif(netif);
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to set default network interface: %s", esp_err_to_name(error));
        return;
    }

		if (modem_is_powered_up() == ESP_OK)
		{
				ESP_LOGI(LOG_TAG, "Modem is already powered up.");
		}
		else
		{

			ESP_LOGI(LOG_TAG, "Modem is not powered up. Powering up the modem...");

			// 5. Power up the modem
			error = modem_power_up();
			if (error != ESP_OK)
			{
					ESP_LOGE(LOG_TAG, "Failed to power up modem: %s", esp_err_to_name(error));
					return;
			}
		
		}

    error = modem_init("internet");
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize modem: %s", esp_err_to_name(error));
        return;
    }

    ESP_LOGI(LOG_TAG, "Modem initialized successfully.");
}
