# Infrastructure Docs

Documentation for `vehicle-tracker-infrastructure`, the part of the project that deploys the vehicle tracker services to the VPS.

The services are described with Podman Compose and deployed with Ansible from GitHub Actions. The VPS runs in a homelab, so the workflow reaches it through a WireGuard tunnel and then connects over SSH.

## Contents

- [Overview](overview.md): the research and design for automating the infrastructure. It covers the deployment flow, the repository layout, the Podman Compose setup and how the CI/CD workflows deploy only the services that changed.

## Running the playbooks

The playbooks need `ansible-core` and the collections listed in `ansible/requirements.yml`. Run the commands from the `ansible/` directory, because that is where `ansible.cfg` is read.

```bash
cd ansible
ansible-galaxy collection install -r requirements.yml
```

## Deploying the API gateway

Apply the VPS provisioning playbook and verify that the rootless `deploy` user can pull from Harbor. The gateway deployment requires `podman-compose` 1.5.0 or newer for service-specific updates. The production inventory must include Asset Service and Identity Service definitions under `services.<name>.compose`. Both containers must already be healthy in the `vehicle-tracker` Compose project on its `backend` network. Deploy from `/home/deploy/vehicle-tracker`.

Set these GitHub Actions secrets for the `production` environment.

| Secret | Value |
| --- | --- |
| `WG_CONFIG_FILE` | WireGuard client configuration with a route to the VPS |
| `VPS_SSH_KEY` | Private SSH key for the VPS deploy user |
| `VPS_SSH_KNOWN_HOSTS` | Verified SSH host key entry for `10.10.10.40` |
| `KEYCLOAK_ISSUER_URI` | HTTPS realm URL matching the token issuer and reachable from the gateway |

After the backend build publishes the image, dispatch **Deploy API Gateway** with its 10-character commit tag. The workflow reports the tag, image ID, and health state. From a client on the private Compose network, use a production Keycloak token to request `GET /assets/devices` and `GET /identity/v3/api-docs` through the gateway. Record the results with the workflow run. To roll back an update, dispatch the workflow with the previous healthy tag.

Run the deployment fixture on a Linux host with rootless Podman, the same Compose provider version as production, and Ansible with PyYAML.

```bash
bash vehicle-tracker-infrastructure/ansible/tests/gateway-deployment.sh
```
