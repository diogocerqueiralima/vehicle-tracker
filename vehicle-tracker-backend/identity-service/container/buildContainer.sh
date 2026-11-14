#!/usr/bin/bash
set -euo pipefail

IMAGE_NAME="identity-service"

podman build \
  --platform linux/amd64 \
  -t $IMAGE_NAME:ci \
  -f Containerfile \
  ../..
