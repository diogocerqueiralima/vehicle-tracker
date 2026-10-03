# Configuration

Configuration is the set of values a device needs to work with the vehicle-tracker system, such as the MQTT broker it connects to, how often it updates its location, and the credentials it authenticates with. The device produces and publishes [reported data](../data/overview.md) continuously, while configuration comes from the mobile application over [BLE](../ble/overview.md) and rarely changes.

Configuration has to survive reboots and power loss, so the device keeps it in non-volatile storage, using the [storage abstraction](../storage/overview.md) to persist each value. The storage partition is encrypted, which protects credentials and other sensitive settings at rest.

In this document, we will explore the configuration system, which is designed to manage and store configuration data efficiently on devices.

## Sections

Configuration values are grouped into sections, one for each area of the device's behavior:

| Section | Description | Document |
|---|---|---|
| **Connection** | MQTT connection settings, such as the broker URL, keep-alive interval, QoS level, and reconnect interval. | [Connection Configuration](connection.md) |
| **GPS** | GPS behavior, such as the location update frequency, fix timeout, and fix strategy. | [GPS Configuration](gps.md) |
| **Authentication** | Credentials used to connect to the MQTT broker, such as the device certificate, the trusted CA certificate, and the certificate expiration. | [Authentication Configuration](authentication.md) |

## Default Values

Each section document lists a default value for the settings that have one. On every boot, the device writes those defaults to storage for any setting that holds no value yet, so a freshly flashed device answers a read with the documented default instead of an error. The device never overwrites a setting that already holds a value, so each default is applied at most once.

Settings documented with a `-` default, such as `broker_url` or the certificates, have no sensible fallback and stay unconfigured until a client writes a value.
