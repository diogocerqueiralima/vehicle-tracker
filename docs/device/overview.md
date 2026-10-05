# Device Overview

This document provides a high-level overview of the devices used in the vehicle-tracker system. Devices are responsible for tracking the location of vehicles and sending the data to the system.

> **Note**: This system is still under development. More information may be added in the future.

## Project structure

The firmware lives in `vehicle-tracker-embedded`. The code is split into components, each with its own source files. The `main` folder only holds the entry point, and `main-test` is a separate app that holds all the tests and builds them together with the components.

```
vehicle-tracker-embedded/
├── CMakeLists.txt
├── sdkconfig
├── main/
│   ├── CMakeLists.txt
│   └── main.c
├── main_test/
│   ├── CMakeLists.txt
│   └── main/
│       ├── CMakeLists.txt
│       ├── main.c
│       └── <component>/
│           └── <component>_test.c
└── components/
    └── <component>/
        ├── CMakeLists.txt
        ├── <file>.h
        └── <file>.c
```

## Storage

Devices use the NVS (Non-Volatile Storage) system with encryption to manage and store data efficiently. For more information on the storage system, refer to the [Storage Overview](storage/overview.md).

## Configuration

Devices receive their configuration, such as MQTT connection, GPS and authentication settings, from the mobile application over BLE and keep it in storage. For more information on the configuration system, refer to the [Configuration Overview](config/overview.md).

## Authentication

Devices authenticate with the MQTT broker using certificates issued by the Identity Service. The authentication process ensures that only authorized devices can send data to the system. For more information on the authentication process, refer to the [Device Authentication Overview](authentication/overview.md).

## Tests

The firmware is covered by unit and integration tests. For more information on the testing approach, refer to the [Device Tests](tests/overview.md).

## Communication

### BLE

Devices expose a BLE (Bluetooth Low Energy) interface that allows authorized clients to interact with the device. For more information on the BLE protocol, refer to the [BLE Overview](ble/overview.md).
