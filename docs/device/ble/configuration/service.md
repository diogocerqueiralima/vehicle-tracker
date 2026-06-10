# Configuration Service

This document describes the BLE GATT service used to configure devices in the vehicle-tracker system, defining the service structure and characteristics.

> **Note**: This document is still under development. The protocol may change as the system evolves.

## Overview

The Configuration Service allows authorized clients to read and write device configuration parameters via BLE. Characteristic values are raw primitive types.

For a full list of configuration parameters, refer to the [Device Configuration Overview](overview.md).

## GATT Service Structure

**Service UUID**: `6578878c-85c9-4566-9a51-37c9aaea0413`

| Characteristic Name | UUID | Type | Actions |
|---|---|---|---|
| `broker_url` | `002af440-7157-45f5-a96f-0c87fa0e5e75` | `string` | Read, Write |
| `keep_alive` | `193bdbf3-3f36-4bd1-a06f-e8bc31f1978f` | `int` | Read, Write |
| `qos` | `497378ba-0252-4d84-a07f-05a886d01186` | `int` | Read, Write |
| `recon_interval` | `1472f295-a20b-4c4b-b191-7f67b0ecaa7c` | `int` | Read, Write |
| `topic` | `9e0ab42d-c0c9-4235-b3a1-79054cf92bc5` | `string` | Read, Write |
| `gps_update` | `2691f217-5921-4b93-938f-23a2186158a7` | `int` | Read, Write |
| `gps_timeout` | `d1832cf4-f6da-4f19-9342-f51e471656ca` | `int` | Read, Write |
| `csr` | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` | `string` | Read |
| `certificate` | `615551ac-42a4-48e8-b6d8-7b108d265cdf` | `string` | Write |
