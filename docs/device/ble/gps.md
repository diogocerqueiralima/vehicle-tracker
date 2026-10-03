# GPS Service

This document provides the parameters and configuration details for the GPS Service, which is responsible for managing the device's GPS behavior, such as update frequency and fix timeout.

## Structure

**Service UUID**: `6ed54c3d-ca79-1398-7148-34a9bf29d12d`

| Name | UUID | Type | Actions | Description | Default Value |
|---|---|---|---|---|---|
| gps_update | a7586118-a223-8f13-930b-215917f29126 | integer | read, write | The frequency in seconds at which the device updates its GPS location. Updates only if the device is moving. | 60s |
| gps_timeout | ca561647-1ef5-4213-190f-daf6f42c83d1 | integer | read, write | The maximum time in seconds the device will wait for a GPS fix before giving up and sending an error message. | 60s |
| gps_mode | d67ed5fc-a8db-c68c-2544-e5ed59f356c6 | string | read, write | fix strategy for the GPS. Possible values are: `standalone`, `ue-based`, `ue-assisted`. | `ue-based` | 
