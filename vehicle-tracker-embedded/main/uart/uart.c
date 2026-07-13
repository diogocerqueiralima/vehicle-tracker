#include "uart.h"
#include <stdlib.h>

#include "driver/uart.h"
#include "freertos/queue.h"

uart_registry_t uart_registry;

/**
 *
 * @brief Removes the UART context for the specified UART port from the UART registry and returns it.
 *
 * @param port the UART port number for which the context should be removed
 * @return A pointer to the removed UART context, or nullptr if no context was found for the specified port
 */
uart_context_t *remove_uart_context(const uart_port_t port)
{

    for (int i = 0; i < uart_registry.count; i++)
    {

        // 1. Get the current context
        uart_context_t *context = uart_registry.contexts[i];

        // 2. Check if the context's port matches the specified port
        if (context->port == port)
        {

            // 3. Remove the context from the registry by shifting the remaining contexts down
            for (int j = i; j < uart_registry.count - 1; j++)
            {
                uart_registry.contexts[j] = uart_registry.contexts[j + 1];
            }

            // 4. Decrement the count of contexts in the registry and return the removed context
            uart_registry.count--;
            return context;
        }
    }

    return nullptr;
}

/**
 *
 * @brief Cleans up the UART context by deleting the UART driver, freeing the associated queue, and releasing the memory allocated for the context itself.
 *
 * @param context A pointer to the UART context to be cleaned up.
 * @return ESP_OK on success, or an appropriate error code if the UART driver deletion fails.
 */
esp_err_t cleanup_uart_context(uart_context_t *context)
{

    // 1. Delete the UART driver for the specified port and check for errors
    const esp_err_t error = uart_driver_delete(context->port);

    // 2. Free the memory allocated for the UART context and its associated queue
    free(context->queue);
    free(context);

    return error;
}

esp_err_t init_uart()
{
    uart_registry.capacity = 10;
    uart_registry.count = 0;
    uart_registry.contexts = malloc(uart_registry.capacity * sizeof(uart_context_t *));

    if (uart_registry.contexts == NULL)
    {
        return ESP_ERR_NO_MEM;
    }

    return ESP_OK;
}

esp_err_t open_uart(const uart_port_t port, const int queue_size, const int buffer_size, const int baud_rate, const int tx, const int rx)
{

    // 1. Check if the UART registry has reached its capacity
    if (uart_registry.count >= uart_registry.capacity)
    {
        return ESP_ERR_NO_MEM;
    }

    // 2. Check if the UART context for the specified port already exists in the registry
    const uart_context_t *existing = get_uart_context(port);
    if (existing != nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 3. Create a new queue for UART events and check for memory allocation failure
    QueueHandle_t *queue = malloc(sizeof(QueueHandle_t));
    if (queue == NULL)
    {
        return ESP_ERR_NO_MEM;
    }

    // 4. Install the UART driver with the specified parameters and check for errors
    esp_err_t status = uart_driver_install(port, buffer_size, buffer_size, queue_size, queue, 0);
    if (status != ESP_OK)
    {
        free(queue);
        return status;
    }

    // 5. Configure the UART parameters (baud rate, data bits, parity, stop bits, flow control) and check for errors
    const uart_config_t uart_config = {
        .baud_rate = baud_rate,
        .data_bits = UART_DATA_8_BITS,
        .parity = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
    };

    status = uart_param_config(port, &uart_config);
    if (status != ESP_OK)
    {

        const esp_err_t error = uart_driver_delete(port);
        if (error != ESP_OK)
        {
            ESP_LOGE(TAG, "Failed to delete UART driver for port %d: %s", port, esp_err_to_name(error));
        }

        free(queue);
        return status;
    }

    // 6. Set the UART pins for TX and RX and check for errors
    status = uart_set_pin(port, tx, rx, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    if (status != ESP_OK)
    {

        const esp_err_t error = uart_driver_delete(port);
        if (error != ESP_OK)
        {
            ESP_LOGE(TAG, "Failed to delete UART driver for port %d: %s", port, esp_err_to_name(error));
        }

        free(queue);
        return status;
    }

    // 7. Create a new UART context with the provided ID and queue, and add it to the UART registry
    uart_context_t *uart_context = malloc(sizeof(uart_context_t));

    if (uart_context == NULL)
    {

        const esp_err_t error = uart_driver_delete(port);
        if (error != ESP_OK)
        {
            ESP_LOGE(TAG, "Failed to delete UART driver for port %d: %s", port, esp_err_to_name(error));
        }

        free(queue);
        return ESP_ERR_NO_MEM;
    }

    uart_context->port = port;
    uart_context->queue = queue;
    uart_registry.contexts[uart_registry.count] = uart_context;
    uart_registry.count++;

    return ESP_OK;
}

esp_err_t close_uart(const uart_port_t port)
{

    // 1. Remove the UART context for the specified port from the registry
    uart_context_t *context = remove_uart_context(port);
    if (context == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    return cleanup_uart_context(context);
}

uart_context_t *get_uart_context(const uart_port_t port)
{

    for (int i = 0; i < uart_registry.count; i++)
    {

        // 1. Get the current context
        uart_context_t *context = uart_registry.contexts[i];

        // 2. Check if the context's port matches the specified port and return it
        if (context->port == port)
        {
            return context;
        }

    }

    return nullptr;
}

void cleanup_uart()
{

    for (int i = 0; i < uart_registry.count; i++)
    {

        uart_context_t *context = uart_registry.contexts[i];
        const esp_err_t error = cleanup_uart_context(context);

        if (error != ESP_OK)
        {
            ESP_LOGE(TAG, "Failed to clean UART context for port %d: %s", context->port, esp_err_to_name(error));
        }

    }

    free(uart_registry.contexts);
    uart_registry.contexts = NULL;
    uart_registry.count = 0;
    uart_registry.capacity = 0;
}

esp_err_t uart_write(const uart_context_t *context, const char *data, const size_t size)
{

    // 1. Check for null context or data, or non-positive size
    if (context == nullptr || data == nullptr || size <= 0)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Get the UART port and start totalWritten with zero
    const uart_port_t port = context->port;
    size_t totalWritten = 0;

    // 3. Write data to the UART port in a loop until all bytes are written or an error occurs
    while (totalWritten < size)
    {

        // 4. Write the remaining data to the UART port and check for errors
        const int written = uart_write_bytes(port, data + totalWritten, size - totalWritten);
        if (written < 0)
        {
            return ESP_FAIL;
        }

        totalWritten += written;
    }

    // 5. Wait for the UART transmission to complete and return the result
    return uart_wait_tx_done(port, pdMS_TO_TICKS(1000));
}

char *uart_read_exact(const uart_port_t port, size_t *size, esp_err_t *status)
{

    // 1. Allocate memory for the data buffer based on the requested size, plus a null terminator
    const size_t requested = *size;
    char *data = malloc(requested + 1);
    if (data == nullptr)
    {
        *status = ESP_ERR_NO_MEM;
        return nullptr;
    }

    // 2. Read from the UART port in a loop until the requested number of bytes is read, the timeout elapses, or an error occurs
    size_t totalRead = 0;
    while (totalRead < requested)
    {

        // 3. Read the remaining bytes from the UART port with a timeout of 1000 ms and check for errors
        const int read = uart_read_bytes(port, data + totalRead, requested - totalRead, pdMS_TO_TICKS(1000));
        if (read < 0)
        {
            *status = ESP_FAIL;
            free(data);
            return nullptr;
        }

        totalRead += read;
    }

    data[totalRead] = '\0';

    *status = ESP_OK;
    return data;
}

char *uart_read(const uart_context_t *context, size_t *size, esp_err_t *status)
{

    // 1. Validate input parameters
    if (context == nullptr || size == nullptr)
    {
        return nullptr;
    }

    // 2. Get the number of bytes currently available in the UART buffer and check for errors
    *status = uart_get_buffered_data_len(context->port, size);
    if (*status != ESP_OK)
    {
        return nullptr;
    }

    // 3. If there are no bytes available, return nullptr
    if (*size == 0)
    {
        return nullptr;
    }

    // 4. Read exactly the available number of bytes from the UART port and return the result
    return uart_read_exact(context->port, size, status);
}

char *uart_read_blocking(const uart_context_t *context, size_t *size, esp_err_t *status, TickType_t timeout)
{

    // 1. Validate input parameters
    if (context == nullptr || size == nullptr || status == nullptr)
    {
        return nullptr;
    }

    // 2. Track elapsed time in a wraparound-safe way
    const TickType_t start = xTaskGetTickCount();

    // 3. Wait for events on the UART queue, handling non-data events internally, until data is read or the timeout is reached
    while ((xTaskGetTickCount() - start) < timeout)
    {

        // 4. Wait for an event from the UART queue with a timeout until the remaining time elapses
        const TickType_t remaining = timeout - (xTaskGetTickCount() - start);
        uart_event_t event;
        if (xQueueReceive(*context->queue, &event, remaining) != pdTRUE)
        {
            break;
        }

        // 5. Handle the received UART event based on its type
        switch (event.type)
        {

            case UART_DATA:
                *size = event.size;
                return uart_read_exact(context->port, size, status);

            case UART_FIFO_OVF:
            case UART_BUFFER_FULL:
                ESP_LOGE(TAG, "UART buffer overflow on port %d, flushing", context->port);
                uart_flush_input(context->port);
                xQueueReset(*context->queue);
                break;

            default:
                ESP_LOGW(TAG, "Unhandled UART event type %d on port %d", event.type, context->port);
                break;
        }
    }

    // 6. Timeout elapsed with no data received
    *status = ESP_ERR_TIMEOUT;
    return nullptr;
}