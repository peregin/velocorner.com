#!/usr/bin/env bash

CONTAINER_REPO="velo_repo"
if [[ $(docker inspect -f '{{.State.Running}}' $CONTAINER_REPO) = "true" ]]; then
  echo "$CONTAINER_REPO is already running ..."
else
  echo home directory is "$HOME"
  docker run -d -p 5492:5432 \
    --rm --name $CONTAINER_REPO \
    --health-cmd='stat /etc/passwd || exit 1' \
    --health-interval=30s \
    -e POSTGRES_DB=velocorner \
    -e POSTGRES_USER=velocorner \
    -e POSTGRES_PASSWORD=velocorner \
    -v "$HOME"/Downloads/psql/velocorner:/var/lib/postgresql/data \
    postgres:12.2-alpine
  echo "$CONTAINER_REPO has been started ..."
fi
