# GPS Service

The GPS Service exposes the device's GPS configuration over BLE. This document lists its UUIDs, types and allowed actions. For what each characteristic means and its default value, refer to the [GPS Configuration](../../config/gps.md).

## Structure

**Service UUID**: `6ed54c3d-ca79-1398-7148-34a9bf29d12d`

| Name | UUID | Type | Actions |
|---|---|---|---|
| gps_update | a7586118-a223-8f13-930b-215917f29126 | integer | read, write |
| gps_timeout | ca561647-1ef5-4213-190f-daf6f42c83d1 | integer | read, write |
| gps_mode | d67ed5fc-a8db-c68c-2544-e5ed59f356c6 | string | read, write |
