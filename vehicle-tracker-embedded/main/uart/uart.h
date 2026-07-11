#ifndef UART_H

#define UART_H
#define TAG     "UART"

#include "esp_err.h"
#include "freertos/FreeRTOS.h"
#include "freertos/queue.h"
#include "hal/uart_types.h"

typedef struct
{

    uart_port_t port;
    QueueHandle_t *queue;

} uart_context_t;

typedef struct
{

    uart_context_t **contexts;
    int capacity;
    int count;

} uart_registry_t;

static uart_registry_t uart_registry;


/**
 * @brief Initializes the UART registry
 *
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t init_uart();

/**
 *
 * @brief Opens a UART port with the specified parameters and registers it in the UART registry.
 *
 * @param port the UART port number to open
 * @param queue_size the size of the event queue for UART events
 * @param buffer_size  the size of the UART driver buffer
 * @param baud_rate the baud rate for the UART communication
 * @param tx the GPIO pin number for UART TX
 * @param rx the GPIO pin number for UART RX
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t open_uart(uart_port_t port, int queue_size, int buffer_size, int baud_rate, int tx, int rx);

/**
 *
 * @brief Closes the specified UART port and removes its context from the UART registry.
 *
 * @param port the UART port number to close
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t close_uart(uart_port_t port);

/**
 *
 * @brief Retrieves the UART context for the specified UART port from the UART registry.
 *
 * @param port the UART port number for which to retrieve the context
 * @return a pointer to the UART context if found, or NULL if the context does not exist in the registry
 */
uart_context_t *get_uart_context(uart_port_t port);

/**
 * @brief Cleans up the UART registry and releases any resources associated with it.
 */
void cleanup_uart();

/**
 *
 * @brief Writes data to the UART port. This function sends the specified data of given size through the UART interface.
 *
 * @param context Pointer to the UART context associated with the UART port to which data will be sent.
 * @param data Pointer to the data buffer to be sent over UART.
 * @param size The size of the data to be sent in bytes.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t uart_write(const uart_context_t *context, const char *data, size_t size);

/**
 *
 * @brief Reads data from the UART port.
 *
 * @param context Pointer to the UART context associated with the UART port from which data will be read.
 * @param size Pointer to a size_t variable where the size of the read data will be stored.
 * @param status Pointer to an esp_err_t variable where the status of the read operation will be stored. If the read is successful, this will be set to ESP_OK; otherwise, it will contain the error code.
 * @return A pointer to the buffer containing the read data, or NULL if there is no data. The caller is responsible for freeing the returned buffer after use.
 */
char *uart_read(const uart_context_t *context, size_t *size, esp_err_t *status);

#endif
