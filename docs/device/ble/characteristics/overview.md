# Characteristics Overview

A characteristic is the unit of data in a GATT server: it holds one value that a client can read, write, or both. The device groups characteristics into [services](../overview.md#services), and each service exposes one section of the device's [configuration](../../config/overview.md). Every characteristic is identified by a UUID.

The service documents list each characteristic with its UUID, type and allowed actions.

## Types

The type of a characteristic tells the client how its value is transferred. Most characteristics use the default types defined by the Bluetooth specification, such as integers, strings and booleans, which are read and written in a single request. For more information, refer to the [Bluetooth GATT specification](https://www.bluetooth.com/specifications/gatt/).

The only custom type is the [file characteristic](file-characteristic.md), a value that can be larger than a single ATT packet and is therefore transferred in chunks.

## Actions

A characteristic allows `read`, `write`, or both, as listed in its service document. A request for an action the characteristic does not allow is rejected by the BLE stack. Failed requests are answered with the [error codes](../error-handling.md) of the BLE layer, the same for every characteristic.
