#ifndef AT_H

#define AT_H
#define AT_COMMAND_TIMEOUT_MS   5000

#include "uart/uart.h"

/**
 *
 * @brief Sends an AT command to the specified UART context and waits for a response that contains the expected token
 *
 * @param context A pointer to the UART context to which the AT command will be sent.
 * @param command The AT command string to be sent.
 * @param size The length of the command string.
 * @param expected The expected response string to be received from the UART context.
 * @return ESP_OK if the command was sent and the expected response was received, or an appropriate error code if there was a failure.
 */
esp_err_t send_at_command(const uart_context_t *context, const char *command, size_t size, const char *expected);

/**
 *
 * @brief Sends an AT command to the specified UART context and returns the response as a dynamically allocated string.
 *
 * @param context A pointer to the UART context to which the AT command will be sent.
 * @param command The AT command string to be sent.
 * @param size The length of the command string.
 * @param expected The expected response string to be received from the UART context.
 * @param response_size A pointer to a size_t variable that will be set to the size of the response string.
 * @param status A pointer to an esp_err_t variable that will be set to the status of the operation. It will be set to ESP_OK if the command was sent and the expected response was received, or an appropriate error code if there was a failure.
 * @return A pointer to a dynamically allocated string containing the response from the UART context, or nullptr if there was an error. The caller is responsible for freeing the returned string.
 */
char *send_at_command_with_response(const uart_context_t *context, const char *command, size_t size, const char *expected, size_t *response_size, esp_err_t *status);

/**
 *
 * @brief Sends the "ATE0" command to the specified UART context to disable echoing of commands.
 *
 * @param context A pointer to the UART context to which the "ATE0" command will be sent.
 * @return ESP_OK if the command was sent and the expected response was received, or an appropriate error code if there was a failure.
 */
esp_err_t disable_echo(const uart_context_t *context);

/**
 *
 * @brief Sends the "ATE1" command to the specified UART context to enable echoing of commands.
 *
 * @param context A pointer to the UART context to which the "ATE1" command will be sent.
 * @return ESP_OK if the command was sent and the expected response was received, or an appropriate error code if there was a failure.
 */
esp_err_t enable_echo(const uart_context_t *context);

/**
 *
 * @brief Sends a test AT command to the specified UART context to verify communication with the device.
 *
 * @param context A pointer to the UART context to which the test AT command will be sent.
 * @return ESP_OK if the command was sent and the expected response was received, or an appropriate error code if there was a failure.
 */
esp_err_t at_test(const uart_context_t *context);

#endif