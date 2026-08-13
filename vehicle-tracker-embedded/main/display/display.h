#ifndef DISPLAY_H

#define DISPLAY_H

#include "esp_err.h"
#include "driver/i2c_types.h"
#include "u8g2.h"

typedef struct
{

    i2c_port_num_t port;
    uint16_t device_address;
    uint32_t clk_speed;

} display_config_t;

typedef struct
{

    i2c_port_num_t port;
    uint16_t device_address;
    u8g2_t u8g2;

} display_context_t;

/**
 * @brief Initializes an SSD1306 OLED display over I2C, registering it on the given bus and
 * running the controller's power-up sequence.
 *
 * @param config Pointer to the display configuration (I2C port, device address, clock speed).
 * @param context Pointer to a display_context_t variable where the resulting display context will be stored.
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t init_display(const display_config_t *config, display_context_t *context);

/**
 * @brief Closes the display device, removing it from the I2C bus.
 *
 * @param context Pointer to the display context returned by init_display.
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t close_display(display_context_t *context);

/**
 * @brief Clears the display content.
 *
 * @param context Pointer to the display context returned by init_display.
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t display_clear(display_context_t *context);

/**
 * @brief Draws a single line of text on the display and sends the frame buffer to the device.
 *
 * @param context Pointer to the display context returned by init_display.
 * @param x The horizontal baseline position, in pixels, where the text starts.
 * @param y The vertical baseline position, in pixels, where the text starts.
 * @param text Null-terminated string to draw.
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t display_write_text(display_context_t *context, int x, int y, const char *text);

#endif
