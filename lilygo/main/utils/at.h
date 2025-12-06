#ifndef AT_H
#define AT_H

#include "esp_modem_api.h"
#include "esp_err.h"
#include "esp_log.h"

/**
 * @brief Callback function to print a line of modem response.
 * 
 * @param data Pointer to the response data.
 * @param len Length of the response data.
 * @return esp_err_t ESP_OK on success.
 */
esp_err_t print_line_callback(uint8_t *data, size_t len);

#endif // AT_H