# GPS Configuration

This document describes the GPS configuration, which controls the device's GPS behavior, such as update frequency and fix timeout.

## Settings

| Name | Type | Description | Default Value |
|---|---|---|---|
| gps_update | integer | How often, in seconds, the device updates its GPS location. The device only updates it while moving. | 60 |
| gps_timeout | integer | The maximum time in seconds the device waits for a GPS fix before giving up and sending an error message. | 60 |
| gps_mode | string | The fix strategy for the GPS. Possible values are `standalone`, `ue-based`, and `ue-assisted`. | `ue-based` |

The [GPS Service](../ble/config/gps.md) exposes these settings over BLE.
