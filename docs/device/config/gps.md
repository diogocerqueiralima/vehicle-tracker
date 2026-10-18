# GPS Configuration

This document describes the GPS configuration, which controls the device's GPS behavior, such as update frequency and fix timeout.

## Settings

| Name | Type | Description | Default Value |
|---|---|---|---|
| gps_update | integer | How often, in seconds, the device updates its GPS location. The device only updates it while moving. | 60 |
| gps_timeout | integer | The maximum time in seconds the device waits for a GPS fix before giving up and sending an error message. | 60 |
| gps_mode | string | The fix strategy for the GPS. Possible values are `standalone`, `ue-based`, and `ue-assisted`. | `ue-based` |

A write is refused when the value is not valid: the update frequency and the fix timeout must be greater than zero, and the mode must be one of the listed values.

The [GPS Service](../ble/config/gps.md) exposes these settings over BLE.
