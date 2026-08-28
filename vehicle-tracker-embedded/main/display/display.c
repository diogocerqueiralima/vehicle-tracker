#include "display.h"

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "i2c/i2c.h"

// u8g2 byte buffer for a single I2C transfer.
#define DISPLAY_I2C_BUFFER_SIZE 32

static esp_err_t raw_display_write(const display_context_t *context, const uint8_t *data, size_t size)
{

    // 1. Get the i2c context for the display's port
    i2c_context_t* i2c_context = get_i2c_context(context->port);
    if (i2c_context == nullptr)
    {
        return ESP_ERR_NOT_FOUND;
    }

    // 2. Write the data to the display device
    return i2c_write(i2c_context, context->device_address, data, size);
}

/**
 *
 * @brief u8g2 byte callback for sending data over I2C.
 * This function is called by the u8g2 library to send bytes to the display over I2C.
 * It accumulates the bytes in a buffer and sends them in a single I2C transaction at the end of the transfer.
 *
 * @param u8x8 the u8x8 structure containing the display context
 * @param msg the message indicating the type of operation (send, start transfer, end transfer, etc.)
 * @param arg_int the number of bytes to send (for U8X8_MSG_BYTE_SEND)
 * @param arg_ptr the pointer to the data to send (for U8X8_MSG_BYTE_SEND)
 * @return true (1) on success, false (0) on failure
 */
static uint8_t u8x8_byte_display_i2c(u8x8_t *u8x8, uint8_t msg, uint8_t arg_int, void *arg_ptr)
{

    static uint8_t buffer[DISPLAY_I2C_BUFFER_SIZE];
    static uint8_t buffer_len;

    switch (msg)
    {

    // 1. Accumulate the bytes u8g2 wants to send for the current transfer
    case U8X8_MSG_BYTE_SEND:
        {

        // 1.1 Check if the buffer has enough space for the new data
        if (arg_int > DISPLAY_I2C_BUFFER_SIZE - buffer_len)
        {
            return 0;
        }

            const uint8_t *data = arg_ptr;
            while (arg_int > 0)
            {
                buffer[buffer_len++] = *data;
                data++;
                arg_int--;
            }
        }
        break;

    // 2. Reset the buffer at the start of a new transfer
    case U8X8_MSG_BYTE_START_TRANSFER:
        buffer_len = 0;
        break;

    // 3. Flush the accumulated buffer over I2C at the end of the transfer
    case U8X8_MSG_BYTE_END_TRANSFER:
        {
            const display_context_t *context = u8x8_GetUserPtr(u8x8);
            const esp_err_t error = raw_display_write(context, buffer, buffer_len);
            if (error != ESP_OK)
            {
                return 0;
            }

        }
        break;

    default:
        return 0;

    }

    return 1;
}

/**
 * GPIO and delay callback for the display.
 * This function is called by the u8g2 library to handle display related GPIO and delay operations.
 *
 * @param u8x8  the u8x8 structure containing the display context
 * @param msg the message indicating the type of operation (delay, GPIO, etc.)
 * @param arg_int the integer argument for the operation (e.g., delay time in milliseconds)
 * @param arg_ptr the pointer to any additional data required for the operation
 * @return true (1) on success, false (0) on failure
 */
static uint8_t u8x8_gpio_and_delay_display(u8x8_t *u8x8, uint8_t msg, uint8_t arg_int, void *arg_ptr)
{

    if (msg == U8X8_MSG_DELAY_MILLI)
    {
        vTaskDelay(pdMS_TO_TICKS(arg_int));
    }

    return 1;
}

esp_err_t init_display(const display_config_t *config, display_context_t *context)
{

    // 1. Validate input params
    if (config == nullptr || context == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Get the i2c context for the configured port
    i2c_context_t* i2c_context = get_i2c_context(config->port);
    if (i2c_context == nullptr)
    {
        return ESP_ERR_NOT_FOUND;
    }

    // 3. Add the display as an i2c device on the bus
    i2c_master_dev_handle_t dev_handle;
    const esp_err_t error = i2c_add_device(i2c_context, config->device_address, config->clk_speed, &dev_handle);
    if (error != ESP_OK)
    {
        return error;
    }

    // 4. Populate the display context
    context->port = config->port;
    context->device_address = config->device_address;

    // 5. Set up u8g2 for a 128x64 SSD1306 panel over I2C, in full frame buffer mode.
    u8g2_Setup_ssd1306_i2c_128x64_noname_f(&context->u8g2, U8G2_R0, u8x8_byte_display_i2c, u8x8_gpio_and_delay_display);

    // 6. Set the display context as the user pointer for u8g2, so the I2C callbacks can access it
    u8x8_SetUserPtr(u8g2_GetU8x8(&context->u8g2), context);

    // 7. Run the controller's power-up/init sequence and turn the display on
    u8g2_InitDisplay(&context->u8g2);
    u8g2_SetPowerSave(&context->u8g2, 0);

    // 8. Lower the default contrast: the SSD1306 powers up near max brightness, which blows out fine detail
    u8g2_SetContrast(&context->u8g2, 20);

    // 9. Set the font for the display
    u8g2_SetFont(&context->u8g2, u8g2_font_ncenB08_tr);

    // 10. Clear any leftover content from RAM
    return display_clear(context);
}

esp_err_t close_display(display_context_t *context)
{

    // 1. Validate input context
    if (context == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Get the i2c context for the display's port
    i2c_context_t* i2c_context = get_i2c_context(context->port);
    if (i2c_context == nullptr)
    {
        return ESP_ERR_NOT_FOUND;
    }

    // 3. Remove the display device from the bus
    return i2c_remove_device(i2c_context, context->device_address);
}

esp_err_t display_clear(display_context_t *context)
{

    // 1. Validate input context
    if (context == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Clear the frame buffer and send it to the display
    u8g2_ClearBuffer(&context->u8g2);
    u8g2_SendBuffer(&context->u8g2);

    return ESP_OK;
}

esp_err_t display_write_text(display_context_t *context, int x, int y, const char *text)
{

    // 1. Validate input params
    if (context == nullptr || text == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Draw the text into the frame buffer and send it to the display
    u8g2_ClearBuffer(&context->u8g2);
    u8g2_DrawStr(&context->u8g2, x, y, text);
    u8g2_SendBuffer(&context->u8g2);

    return ESP_OK;
}

esp_err_t display_write_center(display_context_t *context, int size, const uint8_t *modules)
{

    // 1. Validate input params
    if (context == nullptr || modules == nullptr || size <= 0)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Scale up to the largest integer factor that still fits the panel's shorter dimension
    const int panel_dim = u8g2_GetDisplayHeight(&context->u8g2) < u8g2_GetDisplayWidth(&context->u8g2)
                               ? u8g2_GetDisplayHeight(&context->u8g2)
                               : u8g2_GetDisplayWidth(&context->u8g2);
    int scale = panel_dim / size;
    if (scale < 1)
    {
        scale = 1;
    }

    // 3. Center the scaled grid on the panel
    const int x_offset = (u8g2_GetDisplayWidth(&context->u8g2) - size * scale) / 2;
    const int y_offset = (u8g2_GetDisplayHeight(&context->u8g2) - size * scale) / 2;

    // 4. Draw each "on" module as a scale x scale box
    u8g2_ClearBuffer(&context->u8g2);

    // 4.1 Loop through every row
    for (int y = 0; y < size; y++)
    {

        // 4.2 Loop through every column
        for (int x = 0; x < size; x++)
        {

            // 4.3 Draw a filled box for each "on" module, scaled up and offset to center
            if (modules[y * size + x] != 0)
            {
                u8g2_DrawBox(&context->u8g2, x_offset + x * scale, y_offset + y * scale, scale, scale);
            }
        }
    }

    u8g2_SendBuffer(&context->u8g2);

    return ESP_OK;
}
