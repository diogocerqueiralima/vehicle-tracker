#include "i2c.h"

#include "driver/i2c_master.h"

i2c_registry_t i2c_registry;

static i2c_device_t* get_i2c_device(const i2c_device_registry_t *device_registry, uint16_t device_address)
{

    if (device_registry == nullptr)
    {
        return nullptr;
    }

    for (int i = 0; i < device_registry->count; i++)
    {

        i2c_device_t* device = device_registry->devices[i];

        if (device->address == device_address)
        {
            return device;
        }

    }

    return nullptr;
}

esp_err_t init_i2c()
{

    i2c_registry.capacity = 10;
    i2c_registry.count = 0;
    i2c_registry.contexts = malloc(i2c_registry.capacity * sizeof(i2c_context_t *));

    if (i2c_registry.contexts == NULL)
    {
        return ESP_ERR_NO_MEM;
    }

    return ESP_OK;
}

esp_err_t open_i2c(i2c_port_num_t port, int sda, int scl)
{

    // 1. Check if the i2c registry has reached its capacity
    if (i2c_registry.count >= i2c_registry.capacity)
    {
        return ESP_ERR_NO_MEM;
    }

    // 2. Check if there is already a context with the provided port
    const i2c_context_t* existing = get_i2c_context(port);
    if (existing != nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 3. Create the i2c bus config
    const i2c_master_bus_config_t bus_cfg = {
        .clk_source = I2C_CLK_SRC_DEFAULT,
        .i2c_port = port,
        .sda_io_num = sda,
        .scl_io_num = scl,
        .glitch_ignore_cnt = 7,
        .flags = {
            .enable_internal_pullup = true
        }
    };

    // 4. Create the i2c bus with the provided config
    i2c_master_bus_handle_t bus;
    const esp_err_t error = i2c_new_master_bus(&bus_cfg, &bus);
    if (error != ESP_OK)
    {
        return error;
    }

    // 5. Create the context
    i2c_context_t* context = malloc(sizeof(i2c_context_t));
    if (context == NULL)
    {
        i2c_del_master_bus(bus);
        return ESP_ERR_NO_MEM;
    }

    context->port = port;
    context->bus = bus;

    i2c_device_registry_t* device_registry = malloc(sizeof(i2c_device_registry_t));
    if (device_registry == NULL)
    {
        free(context);
        i2c_del_master_bus(bus);
        return ESP_ERR_NO_MEM;
    }

    device_registry->capacity = 10;
    device_registry->count = 0;
    device_registry->devices = malloc(device_registry->capacity * sizeof(i2c_device_t*));

    if (device_registry->devices == NULL)
    {
        free(device_registry);
        free(context);
        i2c_del_master_bus(bus);
        return ESP_ERR_NO_MEM;
    }

    context->device_registry = device_registry;

    // 6. Register the context
    i2c_registry.contexts[i2c_registry.count] = context;
    i2c_registry.count++;

    return ESP_OK;
}

esp_err_t close_i2c(i2c_port_num_t port)
{

    // 1. Search for the context with the given port in the i2c registry
    for (int i = 0; i < i2c_registry.count; i++)
    {

        i2c_context_t* context = i2c_registry.contexts[i];

        if (context->port != port)
        {
            continue;
        }

        // 2. Remove all devices from the bus
        i2c_device_registry_t* device_registry = context->device_registry;
        for (int j = 0; j < device_registry->count; j++)
        {

            i2c_device_t* device = device_registry->devices[j];

            const esp_err_t error = i2c_master_bus_rm_device(device->handle);
            if (error != ESP_OK)
            {
                return error;
            }

            free(device);

        }

        free(device_registry->devices);
        free(device_registry);

        // 3. Delete the i2c bus
        const esp_err_t error = i2c_del_master_bus(context->bus);
        if (error != ESP_OK)
        {
            return error;
        }

        // 4. Remove the context from the registry
        free(context);
        i2c_registry.contexts[i] = i2c_registry.contexts[i2c_registry.count - 1];
        i2c_registry.count--;

        return ESP_OK;

    }

    return ESP_ERR_NOT_FOUND;
}

i2c_context_t* get_i2c_context(i2c_port_num_t port)
{

    for (int i = 0; i < i2c_registry.count; i++)
    {

        // 1. Get the current context
        const i2c_context_t* context = i2c_registry.contexts[i];

        // 2. Check if the context's port matches the specified port and return it
        if (context->port == port)
        {
            return i2c_registry.contexts[i];
        }

    }

    return nullptr;
}

void cleanup_i2c()
{

    // 1. Close every open i2c bus, freeing its context and devices
    while (i2c_registry.count > 0)
    {
        close_i2c(i2c_registry.contexts[0]->port);
    }

    // 2. Free the registry itself
    free(i2c_registry.contexts);
    i2c_registry.contexts = nullptr;
    i2c_registry.capacity = 0;

}

esp_err_t i2c_add_device(i2c_context_t *context, uint16_t device_address, uint32_t clk_speed, i2c_master_dev_handle_t *dev_handle)
{

    // 1. Validate input context
    if (context == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    i2c_device_registry_t* device_registry = context->device_registry;

    // 2. Check if the device registry has reached its capacity
    if (device_registry->count >= device_registry->capacity)
    {
        return ESP_ERR_NO_MEM;
    }

    // 3. Create the device config
    const i2c_device_config_t dev_cfg = {
        .dev_addr_length = I2C_ADDR_BIT_LEN_7,
        .device_address = device_address,
        .scl_speed_hz = clk_speed,
    };

    // 4. Add the device to the bus
    const esp_err_t error = i2c_master_bus_add_device(context->bus, &dev_cfg, dev_handle);
    if (error != ESP_OK)
    {
        return error;
    }

    // 5. Create the device
    i2c_device_t* device = malloc(sizeof(i2c_device_t));
    if (device == NULL)
    {
        i2c_master_bus_rm_device(*dev_handle);
        return ESP_ERR_NO_MEM;
    }

    device->address = device_address;
    device->handle = *dev_handle;

    // 6. Register the device
    device_registry->devices[device_registry->count] = device;
    device_registry->count++;

    return ESP_OK;
}

esp_err_t i2c_write(i2c_context_t *context, uint16_t device_address, const uint8_t *data, size_t size)
{

    // 1. Validate input context
    if (context == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Search for the device with the given address in the context's device registry
    const i2c_device_t* device = get_i2c_device(context->device_registry, device_address);
    if (device == nullptr)
    {
        return ESP_ERR_NOT_FOUND;
    }

    // 3. Write to the device
    return i2c_master_transmit(device->handle, data, size, -1);
}

esp_err_t i2c_read(i2c_context_t *context, uint16_t device_address, uint8_t *data, size_t size)
{

    // 1. Validate input context
    if (context == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Search for the device with the given address in the context's device registry
    const i2c_device_t* device = get_i2c_device(context->device_registry, device_address);
    if (device == nullptr)
    {
        return ESP_ERR_NOT_FOUND;
    }

    // 3. Read from the device
    return i2c_master_receive(device->handle, data, size, -1);

}

esp_err_t i2c_remove_device(i2c_context_t *context, uint16_t device_address)
{

    i2c_device_registry_t* device_registry = context->device_registry;

    // 1. Validate input context
    if (context == nullptr)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Search for the device with the given address in the context's device registry
    for (int i = 0; i < device_registry->count; i++)
    {

        i2c_device_t* device = device_registry->devices[i];

        if (device->address != device_address)
        {
            continue;
        }

        // 3. Remove the device from the bus
        const esp_err_t error = i2c_master_bus_rm_device(device->handle);
        if (error != ESP_OK)
        {
            return error;
        }

        // 4. Remove the device from the registry
        free(device);
        device_registry->devices[i] = device_registry->devices[device_registry->count - 1];
        device_registry->count--;

        return ESP_OK;

    }

    return ESP_ERR_NOT_FOUND;
}
