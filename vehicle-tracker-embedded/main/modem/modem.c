#include "modem.h"
#include "driver/gpio.h"
#include "esp_err.h"
#include "esp_modem_api.h"

#define TAG	"modem"

esp_err_t modem_is_powered_up()
{

	// 1. Configure the power status pin as an input
	gpio_config_t config = {
		.pin_bit_mask = (1ULL << POWER_STATUS_PIN),
		.mode = GPIO_MODE_INPUT,
		.pull_up_en = GPIO_PULLUP_DISABLE,
		.pull_down_en = GPIO_PULLDOWN_DISABLE,
		.intr_type = GPIO_INTR_DISABLE,
	};

	esp_err_t error = gpio_config(&config);
	if (error != ESP_OK)
	{
		return error;
	}

	// 2. Read the level of the power status pin
	int level = gpio_get_level(POWER_STATUS_PIN);
	if (level == 1)
	{
		return ESP_OK;
	}

	return ESP_ERR_INVALID_STATE;
}

esp_err_t modem_power_up()
{

	// 1. Configure the power pin as an output
	gpio_config_t config = {
		.pin_bit_mask = (1ULL << POWER_PIN),
		.mode = GPIO_MODE_OUTPUT,
		.pull_up_en = GPIO_PULLUP_DISABLE,
		.pull_down_en = GPIO_PULLDOWN_DISABLE,
		.intr_type = GPIO_INTR_DISABLE,
	};

	esp_err_t error = gpio_config(&config);
	if (error != ESP_OK)
	{
		return error;
	}

	// 2. Pull the power pin low to power up the modem
	error = gpio_set_level(POWER_PIN, 0);
	if (error != ESP_OK)
	{
		return error;
	}

	vTaskDelay(pdMS_TO_TICKS(POWER_DELAY_MS));

	// 3. Pull the power pin high to complete the power-up sequence
	error = gpio_set_level(POWER_PIN, 1);
	if (error != ESP_OK)
	{
		return error;
	}

	// 4. Configure the power status pin as an input to check if the modem is powered up
	config.pin_bit_mask = (1ULL << POWER_STATUS_PIN);
	config.mode = GPIO_MODE_INPUT;

	error = gpio_config(&config);
	if (error != ESP_OK)
	{
		return error;
	}

	// 5. Wait for the modem to indicate that it is powered up by checking the power status pin
	int tries = 0;
	while (gpio_get_level(POWER_STATUS_PIN) == 0)
	{

		vTaskDelay(pdMS_TO_TICKS(POWER_STATUS_DELAY_MS));
		tries++;

		if (tries > POWER_STATUS_TRIES)
		{
			return ESP_ERR_TIMEOUT;
		}

	}

	return ESP_OK;
}

esp_err_t modem_init(char *apn)
{

    // 1. Get te default network interface for the PPP connection
    esp_netif_t *netif = esp_netif_get_default_netif();
    if (netif == NULL)
    {
    	ESP_LOGE(TAG, "No default netif");
        return ESP_ERR_INVALID_STATE;
    }

    // 2. Configure the DTE (Data Terminal Equipment) for the modem using UART settings
    esp_modem_dte_config_t dte_config = ESP_MODEM_DTE_DEFAULT_CONFIG();
    dte_config.uart_config.port_num = UART_PORT_NUMBER;
    dte_config.uart_config.tx_io_num = TX;
    dte_config.uart_config.rx_io_num = RX;
    dte_config.uart_config.rts_io_num = UART_PIN_NO_CHANGE;
    dte_config.uart_config.cts_io_num = UART_PIN_NO_CHANGE;

    // 3. Configure the DCE (Data Communication Equipment) for the modem with the provided APN
    const esp_modem_dce_config_t dce_config = ESP_MODEM_DCE_DEFAULT_CONFIG(apn);

    // 4. Create a new modem DCE instance for the SIM7600 module using the configured DTE and DCE settings
    esp_modem_dce_t *modem = esp_modem_new_dev(ESP_MODEM_DCE_SIM7600, &dte_config, &dce_config, netif);
    if (modem == NULL)
    {
    	ESP_LOGE(TAG, "Failed to create modem instance");
        return ESP_FAIL;
    }

    // 6. Synchronize the modem to ensure it is ready for communication and properly configured
    esp_err_t error = esp_modem_sync(modem);
    if (error != ESP_OK)
    {
    	ESP_LOGE(TAG, "Failed to synchronize modem");
        return error;
    }

    return ESP_OK;
}
