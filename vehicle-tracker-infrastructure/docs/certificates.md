# Certificates

This document describes the certificates used by the infrastructure: where they live on the VPS, who uses them and how Ansible gives the containers access to them.

## Certificates in use

| Certificate | Purpose | Where it lives |
|---|---|---|
| VPS certificate (`tracker.crt` and `tracker.key`) | Identifies the server. nginx uses it for HTTPS and `mosquitto` uses it for the TLS listener on port 8883. | On the VPS, in `/etc/tracker/certs` |
| Identity Service CA (`identity-service.crt`) | CA of the Identity Service, which signs the certificates of the devices. Used by the broker to authenticate devices. | On the VPS, in `/etc/tracker/certs` |
| Homelab Root CA (`ca.crt`) | Root CA of the homelab. Podman uses it to trust the homelab when it pulls images. | In the repository (`ansible/roles/podman/files/ca.crt`), installed on the VPS in `/etc/containers/certs.d/<homelab_url>` |

The VPS certificate and the Identity Service CA have different roles and must stay separate. The first proves the identity of the server to the clients. The second decides which devices are allowed to connect. If both were the same certificate, anyone with the server key could also sign new devices.

Private keys are never stored in the repository.

## Ansible configuration

The paths and file names are variables in `ansible/inventory/production/group_vars/all.yml`:

```yaml
tls_cert_dir: /etc/tracker/certs
tls_cert_file: tracker.crt
tls_key_file: tracker.key
```

Two places use them:

- **`common` role** (runs in `playbooks/site.yml`, as root): sets the group `deploy` and mode `0640` on the certificate and the key. It fails if the files do not exist.
- **`app` role** (runs in `playbooks/deploy.yml`, as the `deploy` user): checks that both files exist, then mounts them read-only into the `mosquitto` container.

Run `site.yml` once after placing or replacing the files, because only that playbook has root access. The `deploy.yml` playbook cannot change their permissions.

## How the broker uses them

Inside the `mosquitto` container the files are always mounted at the same paths, whatever their names are on the host:

```
/mosquitto/certs/tracker.crt
/mosquitto/certs/tracker.key
/mosquitto/certs/identity-service.crt
```

`mosquitto.conf` points the TLS listener (port 8883) at these paths with `certfile` and `keyfile`. Mosquitto reads them at startup, as root of the container. With rootless Podman that is the `deploy` user on the host, which is why the `deploy` user must be able to read the key.

## Homelab Root CA

The Homelab Root CA is a self-signed root (`CN=Homelab Root CA`, valid from 14 Aug 2026 to 11 Aug 2036). Unlike the other certificates, it is public, so it is committed in the repository and Ansible installs it by itself.

This Homelab Root CA signs the VPS certificate and other certificates in the homelab (e.g. Harbor).