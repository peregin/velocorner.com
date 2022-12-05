#!/usr/bin/env bash

CONTAINER_REPO="velo_minio"
if [[ $(docker inspect -f '{{.State.Running}}' $CONTAINER_REPO) = "true" ]]; then
  echo "$CONTAINER_REPO is already running ..."
else
  echo home directory is "$HOME"
  docker run \
     -d --rm --name $CONTAINER_REPO \
     -p 9010:9000 \
     -p 9090:9090 \
     --name minio \
     -v "$HOME"/Downloads/velo/velocorner/minio/data:/data \
     -e "MINIO_ROOT_USER=admin" \
     -e "MINIO_ROOT_PASSWORD=admin123" \
     quay.io/minio/minio server /data --console-address ":9090"
  echo "$CONTAINER_REPO has been started ..."
fi
