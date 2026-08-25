#!/usr/bin/bash

HARBOR_URL="registry.homelab"
HARBOR_PROJECT="vehicle-tracker"
IMAGE_NAME="asset-service"
IMAGE_TAG=$(tr -dc 'a-z0-9' </dev/urandom | head -c 10)

docker buildx build \
  --platform linux/amd64 \
  -t $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:$IMAGE_TAG \
  -t $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:latest \
  -f Dockerfile \
  --push \
  ../..
