# Certificates

This document describes the certificates used by the infrastructure: where they live on the VPS, who uses them and how Ansible gives the containers access to them.

## Certificates in use

| Certificate | Purpose | Where it lives |
|---|---|---|
| VPS certificate (`tracker.crt` and `tracker.key`) | Identifies the server. nginx uses it for HTTPS and `mosquitto` uses it for the TLS listener on port 8883. | On the VPS, in `/etc/tracker/certs` |
| Identity Service CA (`identity-service.crt`) | CA of the Identity Service, which signs the certificates of the devices. Its private key stays inside the TPM and only the Identity Service can use it to sign. The broker only has the public certificate, to verify the devices. | On the VPS, in `/etc/tracker/certs` |
| Homelab Root CA (`ca.crt`) | Root CA of the homelab. Podman uses it to trust the homelab when it pulls images. | In the repository (`ansible/roles/podman/files/ca.crt`), installed on the VPS in `/etc/containers/certs.d/<homelab_url>` |

The VPS certificate and the Identity Service CA have different roles and must stay separate. The first proves the identity of the server to the clients. The second decides which devices are allowed to connect. If both were the same certificate, anyone with the server key could also sign new devices.

Private keys are never stored in the repository.

## Identity Service CA key

The private key of the Identity Service CA is created inside the TPM (through `tpm2-pkcs11`) and cannot be exported. Only the Identity Service uses it, to sign the certificates of the devices. Nothing else signs with it, including the broker.

The broker only receives the public certificate (`identity-service.crt`), which is enough to verify that a device certificate was signed by this CA. It cannot sign anything with it. This means that even if someone takes control of the broker, they cannot create certificates that other devices or the broker would accept.

## Ansible configuration

The paths and file names are variables in `ansible/inventory/production/group_vars/all.yml`:

```yaml
tls_cert_dir: /etc/tracker/certs
tls_cert_file: tracker.crt
tls_key_file: tracker.key
identity_ca_cert_file: identity-service.crt
```

Two places use them:

- **`common` role** (runs in `playbooks/site.yml`, as root): sets the group `deploy` and mode `0640` on the certificate and the key. It fails if the files do not exist.
- **`app` role** (runs in `playbooks/deploy.yml`, as the `deploy` user): checks that the three files exist (certificate, key and Identity Service CA), then mounts them read-only into the `mosquitto` container.

Run `site.yml` once after placing or replacing the files, because only that playbook has root access. The `deploy.yml` playbook cannot change their permissions.

## How the broker uses them

Inside the `mosquitto` container the files are always mounted at the same paths, whatever their names are on the host:

```
/mosquitto/certs/tracker.crt
/mosquitto/certs/tracker.key
/mosquitto/certs/identity-service.crt
```

`mosquitto.conf` configures the TLS listener (port 8883) with mutual TLS:

- `certfile` and `keyfile` (`tracker.crt` and `tracker.key`): the certificate the broker sends to the devices, so they can verify the server.
- `cafile` (`identity-service.crt`): the CA the broker uses to verify the certificate of each device.
- `require_certificate true`: a device without a certificate signed by the Identity Service CA fails the TLS handshake.
- `use_identity_as_username true`: the CN of the device certificate becomes the MQTT username, so no password file is needed.
- `allow_anonymous false`: connections without an identity are refused.

Mosquitto reads them at startup. The `mosquitto` container runs with `userns_mode: keep-id` and as the uid of the `deploy` user, so the process has the same identity inside and outside the container and does not drop to an unprivileged subuid. This is why the `deploy` user must be able to read the key (`0640`, group `deploy`), and why the Mosquitto config and ACL can be private to the `deploy` user (`0600`). The Identity Service CA is public, so it is exported by the `tpm` role with mode `0644`.

## Homelab Root CA

The Homelab Root CA is a self-signed root (`CN=Homelab Root CA`, valid from 14 Aug 2026 to 11 Aug 2036). Unlike the other certificates, it is public, so it is committed in the repository and Ansible installs it by itself.

This Homelab Root CA signs the VPS certificate and other certificates in the homelab (e.g. Harbor).
## Open points

### Revocation of device certificates

How the broker can know whether a certificate presented by a device was revoked is still undefined.
