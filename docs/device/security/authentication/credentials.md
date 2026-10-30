# Credentials

The system uses a single certificate issued by the Identity Service. This certificate is used for authenticating the device with the MQTT broker and other services.

This document covers how the device handles its credentials: the key pair, the CSR and the certificate. For the states the device goes through, from enrollment to renewal, expiry and revocation, refer to the [Lifecycle](../lifecycle.md). For the end-to-end flow with the Identity Service, refer to [Device Authentication](./overview.md).

## Device Key and CSR

The device creates its own key pair. The private key proves that the device owns its certificate, so it is the part that must never leave the device.

- **Creating a request** happens only when the user starts the enrollment explicitly, never as a side effect of reading. Starting an enrollment generates a new NIST P-256 key pair in the device's key store, replacing any key that is there, and signs the request with it. A request for a renewal is described in [Renewing a certificate](#renewing-a-certificate). The common name is the device identifier (see [Security](../overview.md#identity)).
- **The private key** is created without export permission and never leaves the key store. Only the request can be read.
- **The request is stored**, so every read returns the same one, and reading never changes the state of the device or its keys. A PEM-encoded P-256 CSR is around 480 bytes, larger than a single BLE packet, so the client reads it in several chunks, and all of them must come from the same request.
- **The request is kept** only until a certificate is installed. While the device is `ACTIVE` or `RENEWING`, starting an enrollment is refused, because a new request would replace the key the certificate depends on.

## Operations by state

The security layer decides what the device allows, based on its [Lifecycle](../lifecycle.md) state, which it keeps in flash.

| State | Starting the enrollment | Reading the request | Writing a certificate |
|---|---|---|---|
| `UNPROVISIONED`, `EXPIRED`, `REVOKED` | Creates a new key and request and moves to `ENROLLING`. | Refused. | Refused. |
| `ENROLLING` | Does nothing, the stored key and request are kept. | Returns the stored request. | Stores a valid certificate and moves to `ACTIVE`. An invalid certificate is refused, the request and its key are discarded, and the device moves to `UNPROVISIONED`. |
| `ACTIVE`, `RENEWING` | Refused, because a new request would replace the key the installed certificate depends on. | Refused. | Refused. |

Revoking is only accepted in `ACTIVE` and `RENEWING`, as in the lifecycle. An `EXPIRED` device does not need to be revoked, it only needs to be enrolled again.

## Installing a certificate

The device stores the certificate, then deletes the stored request, and saves `ACTIVE` last. An interruption before the state is saved leaves the device in `ENROLLING`, and the user installs the certificate again.

## Renewing a certificate

The device does not reuse its key for a renewal. It creates a new key pair and a new request, and sends the request to the Identity Service, authenticating with its current certificate. The new key is kept apart, and the current key and certificate stay in use until the new certificate is installed.

When the Identity Service returns the new certificate, the device stores it, replaces the current certificate and key, and saves `ACTIVE`. If the renewal fails, the device stays in `RENEWING` and tries again, until it succeeds or the current certificate expires, and the user can revoke the device meanwhile.

Renewal is only possible while the current certificate is valid. Once it has expired, the device cannot authenticate with the Identity Service and must be enrolled again by the user.

## Revoking

Revoking the credentials takes two actions, because each one covers something the other cannot:

- **On the device, via BLE**: the device deletes the installed certificate, the stored request if there is one, and the private keys they were bound to, including the new key of a renewal in progress. This stops the device from using the credentials.
- **On the Identity Service**: the client asks the Identity Service to revoke the device's certificate. This is what makes the certificate invalid for the other services. The device cannot do it, because its credentials are gone after the local revocation.

Revoking only on the device leaves the old certificate valid, and revoking only on the Identity Service leaves the private key on the device.

The device saves `REVOKED` first, then deletes the credentials. An interruption leaves the device in `REVOKED` with some credentials still on file, and the user revokes again to finish. Any leftover key is replaced when the user enrolls the device again.

Revocation due to compromise is not a common scenario. In practice, it typically happens when the user explicitly revokes the credentials.
