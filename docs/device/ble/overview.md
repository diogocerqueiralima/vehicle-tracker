# BLE Overview

This document provides a high-level overview of the BLE (Bluetooth Low Energy) communication used in the vehicle-tracker system. BLE is used to configure devices through a mobile application.

> **Note**: This system is still under development. More services may be added in the future.

## Authentication

Authentication is handled at the BLE protocol level through the native pairing and bonding mechanism, using a PIN (Passkey). The device will reject any configuration request from unauthenticated clients.

## GATT Server

The device exposes a **GATT Server** that allows authorized clients to read and write configuration parameters. The GATT Server is organized into services, each grouping related characteristics.

## Services

| Service | Description | Document |
|---|---|---|
| **Connection Service** | Allows reading and writing device connection parameters. | [Connection Service](connection.md) |
| **GPS Service** | Allows reading and writing GPS configuration parameters. | [GPS Service](gps.md) |
| **Authentication Service** | Allows reading and writing authentication parameters. | [Authentication Service](authentication.md) |

## File Characteristics

Some characteristics hold a **file**-typed value: one that can be larger than a single ATT packet, so it is transferred in chunks instead of a single read/write. Every chunk, in either direction, is framed as:

```
[total_len: uint32 LE][offset: uint32 LE][payload: total_len - offset bytes, capped to what fits the negotiated MTU]
```

- `total_len` is the size in bytes of the complete value being transferred.
- `offset` is where this chunk's `payload` starts within that value.
- `payload` is that chunk's slice of the value. It is always prefixed with the 8-byte `[total_len][offset]` header above, never sent on its own.

**Reading** a file characteristic returns one chunk per read, starting at `offset` 0. The device keeps the value cached for the duration of the sequence and serves the next `offset` on each subsequent read from the same connection. Reading it again from a fresh connection, or before a previous sequence finished, restarts the sequence from `offset` 0. The client keeps reading until `offset + len(payload)` reaches `total_len`.

**Writing** a file characteristic works the same way in reverse: the first chunk (`offset` 0) declares the `total_len` of the value being sent and starts a new write, discarding any write already in progress on that characteristic. Every following chunk must continue that same write - same connection, same `total_len`, and `offset` equal to the number of bytes already received - or the device rejects it and the client must restart from `offset` 0. The device only validates and persists the value once the last chunk arrives (`offset + len(payload) == total_len`).

Each file characteristic's document lists its own size cap. A write declaring a `total_len` above that cap is rejected outright.

## Configuration

Each service exposes one section of the device's configuration. The BLE documents list each value's UUID, type and allowed actions. The [Configuration](../config/overview.md) documents describe what each value means, its default, and how the device handles it.

## Error Codes

The device answers failed requests with the following error codes. Most are defined by the Bluetooth specification; the application-specific ones are taken from the `0x80`-`0x9F` range the specification reserves for the higher-layer profile.

| Code | Name | Source | Meaning |
|---|---|---|---|
| `0x0D` | Invalid Attribute Value Length | Bluetooth specification | A [file characteristic](#file-characteristics) write chunk is malformed: shorter than the 8-byte `[total_len][offset]` header. |
| `0x0E` | Unlikely Error | Bluetooth specification | A read failed for a reason other than the setting being unconfigured, such as a storage failure. Because this code differs from `0x90`, a client can tell an unconfigured setting apart from a misbehaving device. |
| `0x13` | Value Not Allowed | Bluetooth specification | A [file characteristic](#file-characteristics) chunk sequence, its declared size, or the complete value itself is invalid. |
| `0x90` | Not Configured | Application | The characteristic holds no value on the device yet, and has no [default](../config/overview.md#default-values) to fall back to. This is normal on an unconfigured device, and the client should offer to write a value instead of reporting a failure. |

> **Note**: `0x90` sits in the upper half of the reserved range because Android's Bluetooth stack reuses `0x80`-`0x8F` for errors of its own, which a client could not tell apart from an error sent by the device.