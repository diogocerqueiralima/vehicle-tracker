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
#define AT_COMMAND_MAX_LEN		32

// Longest line the module is expected to report on its own, anything longer is dropped. An NMEA
// sentence takes at most 82 characters and the header of a received message a few more.
#define MODEM_URC_LINE_MAX_LEN	256

#include "esp_err.h"
#include "esp_modem_api.h"

/**
 * @brief Callback invoked with every line the module reports on its own, such as the NMEA sentences
 * of the GNSS engine or the notification of a received message.
 *
 * The callback runs in the task that reads the modem, so it must return quickly and must not issue
 * an AT command with modem_at(), which would block the task that delivers its answer. Work that
 * takes long belongs in a task of its own, handed the line from here.
 *
 * The lines the module writes while it is answering a command are handed over as many times as it
 * takes parts to write that answer, and the line written across two of those parts is handed over in
 * pieces, so a listener takes a line it cannot make sense of, and a line it already saw, for granted.
 *
 * @param line The reported line without its terminator, only valid for the duration of the call.
 * @param arg The opaque argument of the listener the callback belongs to.
 */
typedef void (*modem_urc_cb_t)(const char* line, void* arg);

/**
 * @brief Listener of what the module reports on its own. Fill in the callback and the argument it is
 * invoked with, the modem owns the rest.
 */
typedef struct modem_urc_listener
{
    modem_urc_cb_t callback;            /**< Callback every reported line is handed to. */
    void* arg;                          /**< Opaque argument passed back to the callback. */
    struct modem_urc_listener* next;    /**< Listener that comes after this one, owned by the modem. */
} modem_urc_listener_t;

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

/**
 *
 * @brief Retrieves the modem instance created by modem_init(). AT commands belong in modem_at(),
 * which keeps what the module reports on its own apart from the answers to them, so this is only for
 * what that function does not cover.
 *
 * @return The modem instance, or NULL if the modem was not initialized successfully.
 */
esp_modem_dce_t *modem_get_dce();

/**
 *
 * @brief Issues an AT command on the modem shared by the modules that talk to it, which must already
 * be initialized with modem_init(). Only one command is issued at a time, a caller that finds
 * another one in flight waits for it to be answered.
 *
 * @param command The null-terminated command to issue, without its terminator.
 * @param response Buffer of ESP_MODEM_C_API_STR_BUF_SIZE bytes that receives the answer, or NULL to
 * discard it.
 * @param timeout_ms The maximum number of milliseconds to wait for the answer.
 * @return ESP_OK on success, ESP_ERR_INVALID_STATE if the modem is not initialized, or an
 * appropriate error code on failure.
 */
esp_err_t modem_at(const char *command, char *response, int timeout_ms);

/**
 *
 * @brief Registers a listener of what the module reports on its own, which every reported line is
 * handed to alongside the listeners already registered. The listener must remain valid until it is
 * removed with modem_remove_urc_listener(), so it is not a variable of the caller's stack, and it
 * cannot be registered from a callback of one.
 *
 * @param listener The listener to register, its callback filled in.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG if the listener carries no callback,
 * ESP_ERR_INVALID_STATE if it is already registered, or an appropriate error code on failure.
 */
esp_err_t modem_add_urc_listener(modem_urc_listener_t *listener);

/**
 *
 * @brief Removes a listener registered with modem_add_urc_listener(). A line being reported while
 * this function runs may still reach the listener, so it stops handling what the module reports
 * shortly after this returns rather than the moment it does. It cannot be removed from a callback of
 * a listener.
 *
 * @param listener The listener to remove.
 * @return ESP_OK on success, ESP_ERR_INVALID_ARG if no listener was given, ESP_ERR_NOT_FOUND if it
 * is not registered, or an appropriate error code on failure.
 */
esp_err_t modem_remove_urc_listener(modem_urc_listener_t *listener);

#endif
