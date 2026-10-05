# Security

The device holds sensitive data, such as the private key and certificate it uses to authenticate with the MQTT broker. The security layer groups the mechanisms that protect this data and the device itself, so the rest of the firmware does not have to deal with them directly.

## What needs protection

- **Data at rest**: credentials and configuration stored in flash must not be readable by dumping the flash. This is covered by the encrypted NVS partition described in [Storage](../storage/overview.md).
- **Data in transit**: communication with the MQTT broker and the Identity Service must be encrypted and authenticated. See [Device Authentication](./authentication/overview.md).
- **Firmware integrity**: the device should only run firmware that we have built and signed.

## Identity

Each device has an identity, which is an identifier (id) that is unique to that device. The device generates it on its first boot and keeps it in storage, so it stays the same across reboots.

The identity is used for two things:

- **Registration**: an administrator uses it to register the device in the platform and assign it to a user. The device shows it as a QR code for this purpose.
- **Certificate common name**: the device uses it as the common name of the CSR, so the Identity Service issues a certificate with the same common name. This ties the certificate to the registered device.

The identity is not a secret, but it must not change once it is generated, because the registration and the certificate depend on it. See [Device Authentication](./authentication/overview.md) for the full flow.

## Keys

The device relies on a few keys, each with a different purpose:

- **Device key pair**: a NIST P-256 key pair generated on the device. The public key goes into the CSR, and the private key proves that the device owns its certificate. The private key is created without export permission and never leaves the device. See [Configuration](../config/authentication.md) for how it is generated and revoked.
- **NVS encryption keys**: protect the encrypted NVS partition, which holds the credentials and configuration. See [Storage](../storage/overview.md).
- **Firmware signing key**: used by the build to sign the firmware, so the device can verify it before running it. Only the public part is on the device.

The private keys must never be exposed over BLE, written to logs or stored outside the device.

## Goals of the implementation

- Keep sensitive data protected on the device and out of logs.
- Ensure only trusted firmware can run on the device.
- Provide a single place for the other layers to rely on for security features.
- Report errors clearly, so a failure in a security feature is never silently ignored.

## References

- [ESP32 Security Features](https://docs.espressif.com/projects/esp-idf/en/stable/esp32/security/index.html) - Official documentation for the security features of the ESP32.
