#ifndef I2C_H

#define I2C_H
#define TAG     "I2C"

#include "esp_err.h"
#include "driver/i2c_types.h"

typedef struct
{

    uint16_t address;
    i2c_master_dev_handle_t handle;

} i2c_device_t;

typedef struct
{

    i2c_device_t **devices;
    int capacity;
    int count;

} i2c_device_registry_t;

typedef struct
{

    i2c_port_num_t port;
    i2c_master_bus_handle_t bus;
    i2c_device_registry_t *device_registry;

} i2c_context_t;

typedef struct
{

    i2c_context_t **contexts;
    int capacity;
    int count;

} i2c_registry_t;

extern i2c_registry_t i2c_registry;

/**
 * @brief Initializes the I2C registry
 *
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t init_i2c();

/**
 *
 * @brief Opens an I2C bus with the specified parameters and registers it in the I2C registry.
 *
 * @param port the I2C port number to open
 * @param sda the GPIO pin number for I2C SDA
 * @param scl the GPIO pin number for I2C SCL
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t open_i2c(i2c_port_num_t port, int sda, int scl);

/**
 *
 * @brief Closes the specified I2C bus and removes its context from the I2C registry.
 *
 * @param port the I2C port number to close
 * @return ESP_OK on success, or an appropriate error code on failure
 */
esp_err_t close_i2c(i2c_port_num_t port);

/**
 *
 * @brief Retrieves the I2C context for the specified I2C port from the I2C registry.
 *
 * @param port the I2C port number for which to retrieve the context
 * @return a pointer to the I2C context if found, or NULL if the context does not exist in the registry
 */
i2c_context_t *get_i2c_context(i2c_port_num_t port);

/**
 * @brief Cleans up the I2C registry and releases any resources associated with it.
 */
void cleanup_i2c();

/**
 *
 * @brief Adds an I2C device to the specified bus.
 *
 * @param context Pointer to the I2C context associated with the bus to which the device will be added.
 * @param device_address the 7-bit I2C address of the device
 * @param clk_speed the clock speed for the device, in Hz
 * @param dev_handle Pointer to an i2c_master_dev_handle_t variable where the resulting device handle will be stored.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t i2c_add_device(i2c_context_t *context, uint16_t device_address, uint32_t clk_speed, i2c_master_dev_handle_t *dev_handle);

/**
 *
 * @brief Removes an I2C device previously added with i2c_add_device.
 *
 * @param context Pointer to the I2C context associated with the bus the device was added to.
 * @param device_address the 7-bit I2C address of the device to remove
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t i2c_remove_device(i2c_context_t *context, uint16_t device_address);

/**
 *
 * @brief Writes data to the I2C device.
 *
 * @param context Pointer to the I2C context associated with the bus the device was added to.
 * @param device_address the 7-bit I2C address of the device to write to
 * @param data Pointer to the data buffer to be sent over I2C.
 * @param size The size of the data to be sent in bytes.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t i2c_write(i2c_context_t *context, uint16_t device_address, const uint8_t *data, size_t size);

/**
 *
 * @brief Reads data from the I2C device.
 *
 * @param context Pointer to the I2C context associated with the bus the device was added to.
 * @param device_address the 7-bit I2C address of the device to read from
 * @param data Pointer to the buffer where the read data will be stored.
 * @param size The number of bytes to read.
 * @return ESP_OK on success, or an appropriate error code on failure.
 */
esp_err_t i2c_read(i2c_context_t *context, uint16_t device_address, uint8_t *data, size_t size);

#endif
