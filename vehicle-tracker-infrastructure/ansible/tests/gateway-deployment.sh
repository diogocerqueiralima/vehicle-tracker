#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
fixture_dir=$(mktemp -d)
project="gateway-test-$(basename "$fixture_dir" | tr '[:upper:].' '[:lower:]-')"
fixture_image=docker.io/library/python:3.13-alpine
gateway_image="localhost/$project:0123456789"
export API_GATEWAY_TAG=0123456789
export KEYCLOAK_ISSUER_URI=https://keycloak.example/realms/tracker
export PODMAN_COMPOSE_PROVIDER=podman-compose

compose=(podman compose -p "$project" -f "$fixture_dir/upstreams.yml")
cleanup() {
  podman rm -f "$project"_api-gateway_1 >/dev/null 2>&1 || true
  if [[ -f "$fixture_dir/upstreams.yml" ]]; then
    "${compose[@]}" down || true
  fi
  podman image rm "$gateway_image" >/dev/null 2>&1 || true
  rm -rf -- "$fixture_dir"
}
trap cleanup EXIT

python3 - "$fixture_dir" "$fixture_image" "$gateway_image" <<'PY'
from pathlib import Path
import sys

import yaml

directory = Path(sys.argv[1])
upstream_image, gateway_image = sys.argv[2:]
upstream = {
    "image": upstream_image,
    "command": ["sleep", "infinity"],
    "networks": ["backend"],
    "healthcheck": {
        "test": ["CMD", "test", "!", "-e", "/tmp/unhealthy"],
        "interval": "1s",
        "retries": 1,
    },
}
upstreams = {name: upstream for name in ("asset-service", "identity-service")}
definitions = {"services": upstreams, "networks": {"backend": {}}}
(directory / "upstreams.yml").write_text(yaml.safe_dump(definitions))

inventory = yaml.safe_load(Path("inventory/production/group_vars/all.yml").read_text())
gateway = inventory["services"]["api-gateway"]["compose"]
gateway["image"] = gateway_image.rsplit(":", 1)[0] + ":${API_GATEWAY_TAG}"
gateway["command"] = [
    "sh", "-ec",
    "mkdir -p /www/actuator; echo UP > /www/actuator/health; exec python3 -m http.server --directory /www 8080",
]
gateway["healthcheck"].update({
    "test": ["CMD", "python3", "-c", "from urllib.request import urlopen; urlopen('http://127.0.0.1:8080/actuator/health')"],
    "interval": "1s",
    "start_period": "1s",
})
gateway_only = {"services": inventory["services"]}
(directory / "gateway-only.yml").write_text(yaml.safe_dump(gateway_only))
inventory["services"].update({name: {"compose": definition} for name, definition in upstreams.items()})
(directory / "services.yml").write_text(yaml.safe_dump({"services": inventory["services"]}))
PY

deploy=(ansible-playbook -i localhost, -c local playbooks/deploy.yml \
  -e "deploy_user=$(id -un)" -e "app_project_dir=$fixture_dir" -e "app_project_name=$project")
with_fixtures="@$fixture_dir/services.yml"
expect_failure() {
  local message=$1
  shift
  if "$@" >"$fixture_dir/failure.log" 2>&1 || ! grep -Fq "$message" "$fixture_dir/failure.log"; then
    cat "$fixture_dir/failure.log"
    return 1
  fi
}

expect_failure 'before deploying the gateway' "${deploy[@]}" -e "@$fixture_dir/gateway-only.yml"
expect_failure '10-character SHA' env API_GATEWAY_TAG= "${deploy[@]}" -e "$with_fixtures"
expect_failure 'KEYCLOAK_ISSUER_URI secret' env KEYCLOAK_ISSUER_URI= "${deploy[@]}" -e "$with_fixtures"

podman pull "$fixture_image"
podman tag "$fixture_image" "$gateway_image"
"${compose[@]}" up -d
asset="$project"_asset-service_1
identity="$project"_identity-service_1
gateway="$project"_api-gateway_1
container_id() { podman inspect --type container --format '{{.Id}}' "$1"; }
timeout 30 podman wait --condition=healthy "$asset" "$identity"
asset_id=$(container_id "$asset")
identity_id=$(container_id "$identity")

podman exec "$asset" touch /tmp/unhealthy
timeout 30 podman wait --condition=unhealthy "$asset"
expect_failure 'unhealthy' "${deploy[@]}" -e "$with_fixtures"
podman exec "$asset" rm /tmp/unhealthy
timeout 30 podman wait --condition=healthy "$asset"

printf 'ASSET_SERVICE_TAG=fixture-upstream-tag\n' >"$fixture_dir/.env"
"${deploy[@]}" -e "$with_fixtures"
initial_gateway_id=$(container_id "$gateway")
grep -qx 'ASSET_SERVICE_TAG=fixture-upstream-tag' "$fixture_dir/.env"
grep -qx "API_GATEWAY_TAG=$API_GATEWAY_TAG" "$fixture_dir/.env"
[[ $(stat -c %a "$fixture_dir/env/api-gateway.env") == 600 ]]

"${deploy[@]}" -e "$with_fixtures"
[[ $(container_id "$gateway") == "$initial_gateway_id" ]]

KEYCLOAK_ISSUER_URI=https://keycloak.example/realms/updated "${deploy[@]}" -e "$with_fixtures"
[[ $(container_id "$gateway") != "$initial_gateway_id" ]]
[[ $(container_id "$asset") == "$asset_id" ]]
[[ $(container_id "$identity") == "$identity_id" ]]
printf 'Gateway deployment fixture passed\n'
