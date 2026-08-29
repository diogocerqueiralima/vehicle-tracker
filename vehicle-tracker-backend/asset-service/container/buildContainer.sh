#!/usr/bin/bash
set -euo pipefail

IMAGE_NAME="asset-service"

docker buildx build \
  --platform linux/amd64 \
  -t $IMAGE_NAME:ci \
  -f Dockerfile \
  ../..
