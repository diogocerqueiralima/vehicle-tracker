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
| **Configuration Service** | Allows reading and writing device configuration parameters. | [Configuration Service](configuration/overview.md) |