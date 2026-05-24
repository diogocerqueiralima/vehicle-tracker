# Device Overview

This document provides a high-level overview of the devices used in the vehicle-tracker system. Devices are responsible for tracking the location of vehicles and sending the data to the system.

> **Note**: This system is still under development. More information may be added in the future.

## Authentication

Devices authenticate with the MQTT broker using certificates issued by the Identity Service. The authentication process ensures that only authorized devices can send data to the system. For more information on the authentication process, refer to the [Device Authentication Overview](authentication/overview.md).

## Communication

### BLE

Devices expose a BLE (Bluetooth Low Energy) interface that allows authorized clients to interact with the device. For more information on the BLE protocol, refer to the [BLE Overview](ble/overview.md).