# Connection Configuration

This document describes the connection configuration, which controls how the device connects to the MQTT broker: the broker URL, the keep-alive interval, and other connection settings. Authentication settings are in the [Authentication Configuration](authentication.md).

## Settings

| Name | Type | Description | Default Value |
|---|---|---|---|
| broker_url | string | The URL of the MQTT broker the device connects to. Must start with `mqtt://` or `mqtts://` and have at most 255 characters. | - |
| keep_alive | integer | The keep-alive interval in seconds for the MQTT connection, from 1 to 65535. | 60 |
| qos | integer | The Quality of Service level for MQTT messages (0, 1, or 2). | 0 |
| recon_interval | integer | The interval in seconds between attempts to reconnect to the MQTT broker after the connection is lost, from 1 to 86400. | 30 |

A value outside these limits is rejected with `ESP_ERR_INVALID_ARG` and the stored value does not change. Over BLE it is reported as `0x13`, see [Error Handling](../ble/error-handling.md).

The [Connection Service](../ble/config/connection.md) exposes these settings over BLE.
