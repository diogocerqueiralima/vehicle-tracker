# Storage

Storage is a critical component of any device, providing the necessary space to store data. In this document, we will explore the NVS (Non-Volatile Storage) system with encryption, which is designed to manage and store data efficiently on devices. Other storage options exist on the ESP32, such as SPIFFS and FAT.

## Supported data types

The NVS system supports a variety of data types, including:

1. Integer types (uint8_t, int8_t, uint16_t, int16_t, uint32_t, int32_t, uint64_t, int64_t)
2. Zero-terminated strings (char arrays)
3. Binary blobs (arbitrary binary data)
4. Floating-point types (float, double)

## Partitions

Partitions are regions of the device's flash, defined in the partition table. The NVS system stores its data inside a partition and organizes it into namespaces, up to 254 per partition. The default partition, called `nvs`, is initialized by the `nvs_flash_init()` function, and it will be used for storing all data in this project.

This partition will be encrypted for this project, as it will store sensitive data such as authentication credentials. The encryption protects the data at rest, so it cannot be read by dumping the flash. It does not protect the data from firmware running on the device, which can still read it.

## Namespaces

Namespaces are logical groupings of key-value pairs within a partition. Each namespace can contain multiple key-value pairs, allowing for efficient organization and retrieval of data. When opening a namespace, the user must specify the desired open mode, which determines the level of access granted to the returned handle. The mode belongs to the handle, not the namespace, so the same namespace can be opened read-only in one place and read-write in another. The available open modes are:

- **Read-only**: The handle can only read from the namespace, and any attempts to write to it will result in an error.
- **Read-write**: The handle can both read from and write to the namespace. Erased and overwritten data is only marked as deleted, so the old value can remain in flash.
- **Read-write-purge**: The handle can both read from and write to the namespace, and every update or erase also purges the previous value from flash, so it cannot be recovered.

## Goals of the implementation

The primary goal of this implementation is to provide an abstraction layer for the storage system, allowing for easy access and management of data without requiring knowledge of the underlying storage mechanisms. This abstraction will enable developers to focus on the application logic rather than the intricacies of data storage.

The implementation must ensure the following:

- Operations to read, write, and delete data given a context (key and namespace).
- Support for the supported data types, including integers, strings, binary blobs, and floating-point types. Examples of functions: `storage_get_int`, `storage_set_int`, `storage_get_blob`.
- Support for namespaces with open modes, allowing for read-only, read-write, and read-write-purge access to data.
- Robust error handling, ensuring that any issues encountered during storage operations are properly reported and managed.

## References

- [Storage API](https://docs.espressif.com/projects/esp-idf/en/stable/esp32/api-reference/storage/index.html) - Official documentation for storage options on ESP32.
- [Non-Volatile Storage (NVS)](https://docs.espressif.com/projects/esp-idf/en/stable/esp32/api-reference/storage/nvs_flash.html) - Detailed information about NVS, including its features and usage.
- [Non-Volatile Storage (NVS) Encryption](https://docs.espressif.com/projects/esp-idf/en/stable/esp32/api-reference/storage/nvs_encryption.html) - Information about NVS encryption and how to use it for secure data storage.