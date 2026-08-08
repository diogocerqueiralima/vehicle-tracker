#ifndef MODEM_H

#define MODEM_H

#define UART_PORT_NUMBER		2
#define TX						27
#define RX						26
#define POWER_PIN				4
#define POWER_DELAY_MS			500
#define POWER_STATUS_PIN		34
#define POWER_STATUS_DELAY_MS	16000
#define POWER_STATUS_TRIES		3

#include "esp_err.h"

/**
 *
 * @brief Checks if the modem is powered up by reading the status of the power status GPIO pin.
 *
 * @return ESP_OK if the modem is powered up, or an appropriate error code if it is not.
 */
esp_err_t modem_is_powered_up();

/**
 *
 * @brief Powers up the modem by configuring the necessary GPIO pins and initializing the UART interface.
 *
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t modem_power_up();

/**
 *
 * @brief Initializes the modem with the specified APN (Access Point Name).
 *
 * @param apn The Access Point Name to be used for the modem's PDP context configuration.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t modem_init(char *apn);

#endif
