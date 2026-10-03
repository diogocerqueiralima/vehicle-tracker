# Connection Configuration

This document describes the connection configuration, which controls how the device connects to the MQTT broker: the broker URL, the keep-alive interval, and other connection settings. Authentication settings are in the [Authentication Configuration](authentication.md).

## Settings

| Name | Type | Description | Default Value |
|---|---|---|---|
| broker_url | string | The URL of the MQTT broker the device connects to. | - |
| keep_alive | integer | The keep-alive interval in seconds for the MQTT connection. | 60 |
| qos | integer | The Quality of Service level for MQTT messages (0, 1, or 2). | 0 |
| recon_interval | integer | The interval in seconds between attempts to reconnect to the MQTT broker after the connection is lost. | 30 |

The [Connection Service](../ble/connection.md) exposes these settings over BLE.
