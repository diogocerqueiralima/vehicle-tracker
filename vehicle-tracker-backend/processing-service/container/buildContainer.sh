#!/usr/bin/bash

HARBOR_URL="registry.mytracker.pt"
HARBOR_PROJECT="vehicle-tracker"
IMAGE_NAME="processing-service"
IMAGE_TAG=$(tr -dc 'a-z0-9' </dev/urandom | head -c 10)

docker build \
  -t $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:$IMAGE_TAG \
  -t $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:latest \
  -f Dockerfile \
  ../..

docker push $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:$IMAGE_TAG
docker push $HARBOR_URL/$HARBOR_PROJECT/$IMAGE_NAME:latest