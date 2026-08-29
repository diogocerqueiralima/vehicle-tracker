#!/usr/bin/bash
set -euo pipefail

HARBOR_URL="registry.homelab"
HARBOR_PROJECT="vehicle-tracker"
IMAGE_NAME="ingestion-service"
IMAGE_TAG=${GITHUB_SHA:0:10}

docker buildx build \
  --platform linux/amd64 \
  -t $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:$IMAGE_TAG \
  -t $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:latest \
  -f Dockerfile \
  --load \
  ../..

docker push $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:$IMAGE_TAG
docker push $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:latest
