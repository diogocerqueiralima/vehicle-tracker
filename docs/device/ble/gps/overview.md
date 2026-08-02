# GPS Service

This document provides the parameters and configuration details for the GPS Service, which is responsible for managing the device's GPS behavior, such as update frequency and fix timeout.

## Structure

**Service UUID**: `2dd129bf-a934-4871-9813-79ca3d4cd56e`

| Name | UUID | Type | Actions | Description | Default Value |
|---|---|---|---|---|---|
| gps_update | 2691f217-5921-4b93-938f-23a2186158a7 | integer | read, write | The frequency in seconds at which the device updates its GPS location. Updates only if the device is moving. | 60s |
| gps_timeout | d1832cf4-f6da-4f19-9342-f51e471656ca | integer | read, write | The maximum time in seconds the device will wait for a GPS fix before giving up and sending an error message. | 60s |
| gps_mode | c656f359-ede5-4425-8cc6-dba8fcd57ed6 | string | read, write | fix strategy for the GPS. Possible values are: `standalone`, `ue-based`, `ue-assisted`. | `ue-based` | 
