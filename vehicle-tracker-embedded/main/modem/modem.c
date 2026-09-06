#include "modem.h"
#include "driver/gpio.h"
#include "esp_err.h"
#include "esp_modem_api.h"
#include <pthread.h>

// What the module reports on its own is handed over as unsolicited result codes, which the modem
// only reports when its handler is built in.
#ifndef CONFIG_ESP_MODEM_URC_HANDLER
#error "Enable CONFIG_ESP_MODEM_URC_HANDLER, the modules the modem is shared with report through it"
#endif

#define TAG	"modem"

// Modem instance created by modem_init(), shared with the modules that talk to the modem over AT commands.
static esp_modem_dce_t *dce = nullptr;

// Listeners of what the module reports on its own, registered with modem_add_urc_listener(). The task
// that reads the modem walks the list to report a line while the tasks sharing the modem register and
// remove listeners on it, so it is only touched while the mutex guarding it is held. That mutex is
// held for as long as a line is reported, so a listener is never handed one once it was removed.
static modem_urc_listener_t *urc_listeners = nullptr;
static pthread_mutex_t urc_listeners_mutex = PTHREAD_MUTEX_INITIALIZER;

esp_err_t modem_is_powered_up()
{

	// 1. Configure the power status pin as an input
	const gpio_config_t config = {
		.pin_bit_mask = (1ULL << POWER_STATUS_PIN),
		.mode = GPIO_MODE_INPUT,
		.pull_up_en = GPIO_PULLUP_DISABLE,
		.pull_down_en = GPIO_PULLDOWN_DISABLE,
		.intr_type = GPIO_INTR_DISABLE,
	};

	const esp_err_t error = gpio_config(&config);
	if (error != ESP_OK)
	{
		return error;
	}

	// 2. Read the level of the power status pin
	const int level = gpio_get_level(POWER_STATUS_PIN);
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

/**
 *
 * @brief Reports a line the module wrote on its own to all the listeners that registered with modem_add_urc_listener().
 *
 * @param line the line the module reported, without its terminator
 */
static void report_line(const char *line)
{
	pthread_mutex_lock(&urc_listeners_mutex);

	for (const modem_urc_listener_t *listener = urc_listeners; listener != NULL; listener = listener->next)
	{
		listener->callback(line, listener->arg);
	}

	pthread_mutex_unlock(&urc_listeners_mutex);
}

/**
 *
 * Handles what the module reports on its own, splitting the bytes it reports into the lines it
 * reports them as and handing every one of them to the listeners. A line is written over as many
 * parts as the module takes to write it, so the one being reported is collected across calls until
 * its terminator arrives.
 *
 * @param data the reported bytes, not null-terminated
 * @param len the number of bytes
 * @return ESP_OK, the bytes are collected here so the modem is always free to drop them
 */
static esp_err_t on_urc(uint8_t *data, const size_t len)
{

	static char line[MODEM_URC_LINE_MAX_LEN];
	static size_t line_len = 0;
	static bool line_dropped = false;

	for (size_t i = 0; i < len; i++)
	{
		const char character = (char)data[i];

		// 1. A line ends at its terminator, which the module writes as a carriage return, a line feed
		// or both, so what lies between two of them is only a line when it holds a character.
		if (character == '\r' || character == '\n')
		{
			if (line_len > 0 && !line_dropped)
			{
				line[line_len] = '\0';
				report_line(line);
			}

			line_len = 0;
			line_dropped = false;
			continue;
		}

		// 2. A line longer than anything the module is expected to report is dropped up to its
		// terminator, rather than reported as the parts it would be cut into.
		if (line_dropped)
		{
			continue;
		}

		if (line_len + 1 >= sizeof(line))
		{
			ESP_LOGW(TAG, "Dropped a reported line longer than %d characters", MODEM_URC_LINE_MAX_LEN);

			line_len = 0;
			line_dropped = true;
			continue;
		}

		line[line_len++] = character;
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
    const esp_err_t error = esp_modem_sync(modem);
    if (error != ESP_OK)
    {
    	ESP_LOGE(TAG, "Failed to synchronize modem");
    	esp_modem_destroy(modem);
        return error;
    }

    // 7. Handle what the module reports on its own, which is handed to the listeners the modules
    // sharing the modem register with modem_add_urc_listener()
    const esp_err_t urc_error = esp_modem_set_urc(modem, on_urc);
    if (urc_error != ESP_OK)
    {
    	ESP_LOGE(TAG, "Failed to handle what the module reports: %s", esp_err_to_name(urc_error));
    	esp_modem_destroy(modem);
        return urc_error;
    }

    // 8. Keep the instance so the other modules sharing the modem can issue AT commands on it
    dce = modem;

    return ESP_OK;
}

esp_err_t modem_at(const char *command, char *response, const int timeout_ms)
{
    if (command == NULL)
    {
        return ESP_ERR_INVALID_ARG;
    }

    if (dce == NULL)
    {
    	ESP_LOGE(TAG, "Modem is not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    return esp_modem_at(dce, command, response, timeout_ms);
}

esp_err_t modem_add_urc_listener(modem_urc_listener_t *listener)
{

	// 1. The listener must be valid and carry a callback, which is called with every line the module reports on its own.
    if (listener == NULL || listener->callback == NULL)
    {
        return ESP_ERR_INVALID_ARG;
    }

	// 2. A listener cannot be registered twice, which would hand it the same line twice.
    pthread_mutex_lock(&urc_listeners_mutex);

    for (const modem_urc_listener_t *registered = urc_listeners;
         registered != NULL;
         registered = registered->next)
    {

        if (registered == listener)
        {
        	pthread_mutex_unlock(&urc_listeners_mutex);

        	ESP_LOGE(TAG, "Listener is already registered");
            return ESP_ERR_INVALID_STATE;
        }

    }

    // 3. Add the listener to the front of the list, so it is handed the lines before the ones registered before it.
    listener->next = urc_listeners;
    urc_listeners = listener;

    pthread_mutex_unlock(&urc_listeners_mutex);

    return ESP_OK;
}

esp_err_t modem_remove_urc_listener(modem_urc_listener_t *listener)
{

	// 1. The listener must be valid, which is not the case when it was never registered or was already removed.
    if (listener == NULL)
    {
        return ESP_ERR_INVALID_ARG;
    }

	// 2. Remove the listener from the list, which is singly linked so the pointer to it must be updated.
    pthread_mutex_lock(&urc_listeners_mutex);

    for (modem_urc_listener_t **link = &urc_listeners; *link != NULL; link = &(*link)->next)
    {
        if (*link == listener)
        {

            *link = listener->next;
            pthread_mutex_unlock(&urc_listeners_mutex);

            return ESP_OK;
        }

    }

    pthread_mutex_unlock(&urc_listeners_mutex);

    return ESP_ERR_NOT_FOUND;
}
