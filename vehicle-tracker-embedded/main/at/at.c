#include "at.h"

#include <string.h>

esp_err_t send_at_command(const uart_context_t *context, const char *command, const size_t size, const char *expected)
{

    // 1. Write the AT command to the UART context and check for errors
    esp_err_t error = uart_write(context, command, size);
    if (error != ESP_OK)
    {
        return error;
    }

    // 2. Read the response from the UART context and check for errors
    size_t response_size;
    char *response = uart_read_blocking(context, &response_size, &error, pdMS_TO_TICKS(AT_COMMAND_TIMEOUT_MS));
    if (error != ESP_OK)
    {
        return error;
    }

    // 3. Check if the response is null or does not contain the expected string, and set the error code accordingly
    if (response == nullptr || strstr(response, expected) == nullptr)
    {
        error = ESP_ERR_INVALID_RESPONSE;
    }

    free(response);
    return error;
}

char *send_at_command_with_response(const uart_context_t *context, const char *command, const size_t size, const char *expected, size_t *response_size, esp_err_t *status)
{

    // 1. Write the AT command to the UART context and check for errors
    *status = uart_write(context, command, size);
    if (*status != ESP_OK)
    {
        return nullptr;
    }

    // 2. Read the response from the UART context and check for errors
    char *response = uart_read_blocking(context, response_size, status, pdMS_TO_TICKS(AT_COMMAND_TIMEOUT_MS));
    if (*status != ESP_OK)
    {
        return nullptr;
    }

    // 3. Check if the response is null or does not contain the expected string, and set the error code accordingly
    if (response == nullptr || strstr(response, expected) == nullptr)
    {
        free(response);
        *status = ESP_ERR_INVALID_RESPONSE;
        return nullptr;
    }
    
    return response;
}

esp_err_t disable_echo(const uart_context_t *context)
{
    const char *command = "ATE0\r\n";
    return send_at_command(context, command, strlen(command), "OK");
}

esp_err_t enable_echo(const uart_context_t *context)
{
    const char *command = "ATE1\r\n";
    return send_at_command(context, command, strlen(command), "OK");
}

esp_err_t at_test(const uart_context_t *context)
{
    const char *command = "AT\r\n";
    return send_at_command(context, command, strlen(command), "OK");
}