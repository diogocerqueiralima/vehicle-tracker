# Infrastructure Docs

Documentation for `vehicle-tracker-infrastructure`, the part of the project that deploys the vehicle tracker services to the VPS.

The services are described with Podman Compose and deployed with Ansible from GitHub Actions. The VPS runs in a homelab, so the workflow reaches it through a WireGuard tunnel and then connects over SSH.

## Contents

- [Overview](overview.md): the research and design for automating the infrastructure. It covers the deployment flow, the repository layout, the Podman Compose setup and how the CI/CD workflows deploy only the services that changed.