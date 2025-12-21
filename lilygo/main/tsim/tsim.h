#ifndef TSIM_H
#define TSIM_H

#include <stdbool.h>

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "driver/gpio.h"
#include "esp_modem_api.h"
#include "esp_event.h"
#include "esp_netif.h"
#include "esp_system.h"

#include "../utils/at.h"

#define PWRKEY_PIN  4
#define TX_PIN      27
#define RX_PIN      26

#define TSIM_SUCCESS                    0
#define CREATE_MODEM_ERROR              1
#define SET_MODEM_COMMAND_MODE_ERROR    2
#define SET_MODEM_DATA_MODE_ERROR       3
#define SYNC_MODEM_ERROR                4
#define DISABLE_ECHO_ERROR              5
#define PAUSE_NETWORK_ERROR             6
#define RESUME_NETWORK_ERROR            7
#define UPDATE_TIME_AND_TIMEZONE_ERROR  8
#define SET_NTP_SERVER_ERROR            9
#define UPDATE_NTP_ERROR               10

typedef void (*ppp_event_handler_t)(void *arg, esp_event_base_t event_base, int32_t event_id, void *event_data);

/**
 * @brief Initialize the TSIM modem.
 * 
 * @param modem [out] Pointer to esp_modem_dce_t structure.
 * @param handler [int] PPP event handler function.
 * @return TSIM_SUCCESS on success, error code otherwise.
 */
int tsim_init(esp_modem_dce_t **modem, ppp_event_handler_t handler);

/**
 * @brief Set the modem to data mode.
 * 
 * @return TSIM_SUCCESS on success, error code otherwise.
 */
int tsim_set_data_mode(esp_modem_dce_t *modem);

/**
 * @brief Enable data mode on the modem.
 * 
 * @return TSIM_SUCCESS on success, error code otherwise.
 */
int tsim_pause_data_mode(esp_modem_dce_t *modem);

/**
 * @brief Resume data mode on the modem.
 * 
 * @return TSIM_SUCCESS on success, error code otherwise.
 */
int tsim_resume_data_mode(esp_modem_dce_t *modem);

/**
 * @brief Get a human-readable error message for a given error code.
 * 
 * @param code Error code.
 * @return Corresponding error message string.
 */
char *tsim_get_error_message(int code);

#endif // TSIM_H