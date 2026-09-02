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
| **Connection Service** | Allows reading and writing device connection parameters. | [Connection Service](connection/overview.md) |
| **GPS Service** | Allows reading and writing GPS configuration parameters. | [GPS Service](gps/overview.md) |
| **Authentication Service** | Allows reading and writing authentication parameters. | [Authentication Service](authentication/overview.md) |

## Default Values

Each service document lists a default value for the characteristics that have one. On every boot, the device writes those defaults to its own storage for the characteristics that hold no value yet, so a freshly flashed device answers a read with the documented default instead of an error. A characteristic that already holds a value is never overwritten, so a default is only ever applied once.

Characteristics documented with a `-` default (such as `broker_url` or the certificates) have no sensible value to fall back to, and stay unconfigured until a client writes one.

## Error Codes

Beyond the error codes defined by the Bluetooth specification, the device answers a read with the following application-specific code, taken from the `0x80`-`0x9F` range the specification reserves for the higher-layer profile.

| Code | Name | Meaning |
|---|---|---|
| `0x90` | Not Configured | The characteristic holds no value on the device yet, and has no default to fall back to. Expected on an unconfigured device: the client should offer to write a value instead of reporting a failure. |

A read that fails for any other reason (a storage failure, for instance) keeps answering with the specification's `0x0E` (Unlikely Error), so a client can tell an unconfigured setting apart from a device that is misbehaving.

> **Note**: the code sits in the upper half of the reserved range because Android's Bluetooth stack reuses `0x80`-`0x8F` for errors of its own, which a client could not tell apart from an error sent by the device.
