#include <inttypes.h>

#include "esp_event.h"
#include "esp_log.h"
#include "esp_netif.h"
#include "ble/ble_manager.h"
#include "ble/services/authentication_service.h"
#include "ble/services/connection_service.h"
#include "ble/services/gps_service.h"
#include "modem/modem.h"
#include "storage/storage.h"
#include "i2c/i2c.h"
#include "display/display.h"

#define LOG_TAG "MAIN"

#define DISPLAY_I2C_PORT 0
#define DISPLAY_SDA_PIN 21
#define DISPLAY_SCL_PIN 22
#define DISPLAY_I2C_ADDRESS 0x3C
#define DISPLAY_I2C_CLK_SPEED_HZ 400000

// Registers all the BLE GATT services with the BLE manager. Must be called before ble_manager_init().
static int register_ble_services(void)
{
    int err = ble_manager_register_service(&connection_service_def);
    if (err != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to register connection service: %d", err);
        return err;
    }

    err = ble_manager_register_service(&gps_service_def);
    if (err != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to register GPS service: %d", err);
        return err;
    }

    err = ble_manager_register_service(&authentication_service_def);
    if (err != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to register authentication service: %d", err);
        return err;
    }

    return 0;
}

// Reflects BLE pairing/connection state on the display: the passkey to enter on the peer
// device while pairing, then a connected/waiting message once bonded or disconnected.
static void on_ble_event(ble_manager_event_t event, const void* data, void* arg)
{
    display_context_t* display_context = arg;
    char buf[32];
    esp_err_t error = ESP_OK;

    switch (event)
    {
    case BLE_MANAGER_EVENT_PASSKEY:
        snprintf(buf, sizeof(buf), "Key: %06" PRIu32, *(const uint32_t*)data);
        error = display_write_text(display_context, 0, 12, buf);
        break;
    case BLE_MANAGER_EVENT_CONNECTED:
        error = display_write_text(display_context, 0, 12, "Connected");
        break;
    case BLE_MANAGER_EVENT_DISCONNECTED:
        error = display_write_text(display_context, 0, 12, "Waiting for connection...");
        break;
    }

    if (error != ESP_OK)
    {
        ESP_LOGW(LOG_TAG, "Failed to update display: %s", esp_err_to_name(error));
    }
}

void app_main()
{
    ESP_LOGI(LOG_TAG, "Application started.");

    // 1. Init storage
    esp_err_t error = init_storage();
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize storage: %s", esp_err_to_name(error));
        return;
    }

    // 2.1 Initialize the I2C registry
    error = init_i2c();
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize I2C registry: %s", esp_err_to_name(error));
        return;
    }

    // 2.2 Open the I2C bus the display is connected to
    error = open_i2c(DISPLAY_I2C_PORT, DISPLAY_SDA_PIN, DISPLAY_SCL_PIN);
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to open I2C bus: %s", esp_err_to_name(error));
        return;
    }

    // 2.3 Initialize the SSD1306 display on the I2C bus
    const display_config_t display_config = {
        .port = DISPLAY_I2C_PORT,
        .device_address = DISPLAY_I2C_ADDRESS,
        .clk_speed = DISPLAY_I2C_CLK_SPEED_HZ
    };

    static display_context_t display_context;
    error = init_display(&display_config, &display_context);
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize display: %s", esp_err_to_name(error));
        return;
    }

    // 2.4 Show initial state until a peer connects
    error = display_write_text(&display_context, 0, 12, "Waiting for connection...");
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to write to display: %s", esp_err_to_name(error));
        return;
    }

    // 3. Register BLE services before initializing the manager
    if (register_ble_services() != 0)
    {
        return;
    }

    // 3.1 Wire BLE pairing/connection events to the display
    ble_manager_set_gap_callback(on_ble_event, &display_context);

    // 4. Initialize the BLE manager and start the NimBLE host task
    int ble_error = ble_manager_init();
    if (ble_error != 0)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize BLE manager: %d", ble_error);
        return;
    }

    // 5. Initialize the network interface
    error = esp_netif_init();
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to initialize network interface: %s", esp_err_to_name(error));
        return;
    }

    // 6. Create the default event loop
    error = esp_event_loop_create_default();
    if (error != ESP_OK)
    {
        ESP_LOGE(LOG_TAG, "Failed to create default loop event");
        return;
    }

    // 7. Create a new network interface for the PPP connection
    const esp_netif_config_t esp_netif_config = ESP_NETIF_DEFAULT_PPP();
    esp_netif_t *netif = esp_netif_new(&esp_netif_config);
    if (netif == NULL)
    {
        ESP_LOGE(LOG_TAG, "No network interface found");
        return;
    }

    // 8. Set the default network interface to the newly created PPP interface
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

			// 9. Power up the modem
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
