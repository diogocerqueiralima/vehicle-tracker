# BLE Authentication

The BLE layer lets a client read and write the device's configuration, including its credentials. Without access control, anyone within range could change the MQTT connection, read the CSR or revoke the credentials. This document describes how the device decides which clients are allowed to talk to it. For the layer's role in the firmware, refer to the [BLE Overview](overview.md#access-control).

This is not the same as the [Authentication Service](config/authentication.md), which exposes the credentials the device uses to authenticate with the MQTT broker. This document is about authenticating the client that connects to the device.

## Passkey

The device uses a passkey, a six-digit code that proves the user has physical access to the device. The passkey is generated per pairing and shown on the device's display:

- **Generated per pairing**: when a client starts pairing, the device generates a new random passkey. It is not a fixed value, and it is not stored, so it cannot be read from flash. A passkey is valid for one pairing attempt only, and the device discards it when the attempt ends, whether it succeeds or fails.
- **Shown on the display**: the device shows the passkey on its display while the pairing is in progress, and stops showing it when the attempt ends. Only someone who can see the device at that moment can read it.
- **Never sent over BLE**: the passkey is not advertised and is not carried by any characteristic. The client only sends what the user typed, and the BLE stack checks it.

A fixed passkey would be weaker, because anyone who saw it once, for example on a label or in a photo, could pair at any time. With a new passkey per pairing, seeing an old one is useless.

## Pairing and bonding

Authentication uses the native BLE pairing and bonding mechanism, so the BLE stack checks the passkey and no application code handles it.

1. The user connects to the device with the mobile application.
2. The device asks the client to pair, generates a passkey and shows it on its display.
3. The application asks the user for the passkey, and the user types the one shown on the display.
4. If it matches, the pairing succeeds, the link is encrypted and the device stores the bond.
5. The client can now read and write the characteristics, which is the **Ready** state of the [BLE Lifecycle](lifecycle.md).

A bonded client does not need to type the passkey again on later connections. If the passkey is wrong, or pairing does not finish in time, the device closes the connection and the client reaches no characteristic.

## Goals of the implementation

- Only a user who can see the device's display while pairing can change its configuration.
- Every characteristic requires an authenticated and encrypted link, so an unauthenticated client is rejected before any request reaches the configuration layer.
- Repeated wrong passkeys are limited, so the passkey cannot be guessed by trying every value. Because the passkey changes on every pairing, the limit applies per attempt, and the device must not reuse a passkey after a failed attempt.
- The passkey is never exposed through BLE, logs, advertising data or storage.

## Related documents

- [BLE Lifecycle](lifecycle.md): the states of a connection, including pairing.
- [Security](../security/overview.md): the overall security of the device.
