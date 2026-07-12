#include "storage.h"

#include <string.h>

#include "nvs.h"
#include "nvs_flash.h"

esp_err_t init_storage()
{
    esp_err_t err = nvs_flash_init();
    if (err == ESP_ERR_NVS_NO_FREE_PAGES || err == ESP_ERR_NVS_NEW_VERSION_FOUND)
    {
        // NVS partition was truncated and needs to be erased
        // Retry nvs_flash_init
        ESP_ERROR_CHECK(nvs_flash_erase());
        err = nvs_flash_init();
    }

    return err;
}

esp_err_t cleanup_storage()
{
    return nvs_flash_deinit();
}

esp_err_t save_data(const char* key, const char* value, size_t len)
{
    // 1. Check for null key or key length exceeding maximum allowed size
    if (key == NULL || strlen(key) >= NVS_KEY_NAME_MAX_SIZE)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Check for null value or non-positive length
    if (value == NULL || len <= 0)
    {
        return ESP_ERR_INVALID_ARG;
    }

    nvs_handle_t nvs_handle;

    // 3. Open NVS handle for the specified key in read-write mode
    esp_err_t err = nvs_open(key, NVS_READWRITE, &nvs_handle);
    if (err != ESP_OK)
    {
        return err;
    }

    // 4. Write the value to NVS under the specified key
    err = nvs_set_blob(nvs_handle, key, value, len);
    if (err != ESP_OK)
    {
        nvs_close(nvs_handle);
        return err;
    }

    // 5. Commit the changes to NVS
    err = nvs_commit(nvs_handle);

    // 6. Close the NVS handle
    nvs_close(nvs_handle);
    return err;
}

esp_err_t get_data_size(const char* key, size_t* out_len)
{
    // 1. Check for null key/out_len or key length exceeding maximum allowed size
    if (key == NULL || out_len == NULL || strlen(key) >= NVS_KEY_NAME_MAX_SIZE)
    {
        return ESP_ERR_INVALID_ARG;
    }

    nvs_handle_t nvs_handle;

    // 2. Open NVS handle for the specified key in read-only mode
    esp_err_t err = nvs_open(key, NVS_READONLY, &nvs_handle);
    if (err != ESP_OK)
    {
        return err;
    }

    // 3. Pass NULL buffer to nvs_get_blob to query the stored size
    err = nvs_get_blob(nvs_handle, key, NULL, out_len);

    // 4. Close the NVS handle
    nvs_close(nvs_handle);
    return err;
}

esp_err_t load_data(const char* key, char* value, size_t len)
{
    // 1. Check for null key/value or key length exceeding maximum allowed size
    if (key == NULL || value == NULL || strlen(key) >= NVS_KEY_NAME_MAX_SIZE)
    {
        return ESP_ERR_INVALID_ARG;
    }

    // 2. Check for non-positive length
    if (len <= 0)
    {
        return ESP_ERR_INVALID_ARG;
    }

    nvs_handle_t nvs_handle;

    // 3. Open NVS handle for the specified key in read-only mode
    esp_err_t err = nvs_open(key, NVS_READONLY, &nvs_handle);
    if (err != ESP_OK)
    {
        return err;
    }

    // 4. Read the value from NVS for the specified key into the provided buffer
    err = nvs_get_blob(nvs_handle, key, value, &len);

    // 5. Close the NVS handle
    nvs_close(nvs_handle);
    return err;
}
