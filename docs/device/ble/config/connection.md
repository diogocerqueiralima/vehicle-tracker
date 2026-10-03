# Connection Service

The Connection Service exposes the device's connection configuration over BLE. This document lists its UUIDs, types and allowed actions. For what each characteristic means and its default value, refer to the [Connection Configuration](../config/connection.md).

## Structure

**Service UUID**: `1304eaaa-c937-511a-6605-c9858c877865`

| Name | UUID | Type | Actions |
|---|---|---|---|
| broker_url | 755e0efa-870c-6f29-f505-577140f42a00 | string | read, write |
| keep_alive | 8f97f131-bce8-6f20-d10b-363ff3db3b19 | integer | read, write |
| qos | 8611d086-a805-7f20-840d-5202ba787349 | integer | read, write |
| recon_interval | 7caaecb0-677f-9131-4b0c-0ba295f27214 | integer | read, write |
