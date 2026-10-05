# BLE Authentication

The BLE layer lets a client read and write the device's configuration, including its credentials. Without access control, anyone within range could change the MQTT connection, read the CSR or revoke the credentials. This document describes how the device decides which clients are allowed to talk to it. For the layer's role in the firmware, refer to the [BLE Overview](overview.md#access-control).

This is not the same as the [Authentication Service](config/authentication.md), which exposes the credentials the device uses to authenticate with the MQTT broker. This document is about authenticating the client that connects to the device.

## Passkey

The device uses a passkey, a numeric code that proves the user has physical access to the device. The device generates the passkey itself, and the user must read it from the device to be able to pair. The passkey is never sent over BLE, and it is not advertised.

## Pairing and bonding

Authentication uses the native BLE pairing and bonding mechanism, so the BLE stack checks the passkey and no application code handles it.

1. The user connects to the device with the mobile application.
2. The device asks the client to pair, and the application asks the user for the passkey.
3. The user types the passkey they read from the device.
4. If it matches, the pairing succeeds, the link is encrypted and the device stores the bond.
5. The client can now read and write the characteristics, which is the **Ready** state of the [BLE Lifecycle](lifecycle.md).

A bonded client does not need to type the passkey again on later connections. If the passkey is wrong, or pairing does not finish in time, the device closes the connection and the client reaches no characteristic.

## Goals of the implementation

- Only a user who can read the passkey from the device can change its configuration.
- Every characteristic requires an authenticated and encrypted link, so an unauthenticated client is rejected before any request reaches the configuration layer.
- Repeated wrong passkeys are limited, so the passkey cannot be guessed by trying every value.
- The passkey is never exposed through BLE, logs or advertising data.

## Related documents

- [BLE Lifecycle](lifecycle.md): the states of a connection, including pairing.
- [Security](../security/overview.md): the overall security of the device.
