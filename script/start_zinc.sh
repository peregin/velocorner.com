#!/usr/bin/env bash

CONTAINER_REPO="zinc"
if [[ $(docker inspect -f '{{.State.Running}}' $CONTAINER_REPO) = "true" ]]; then
  echo "$CONTAINER_REPO is already running ..."
else
  echo "starting $CONTAINER_REPO ..."
  docker run -d --rm --name $CONTAINER_REPO \
      -v $HOME/Downloads/velo/velocorner/zinc/data:/data \
      -e ZINC_DATA_PATH="/data" \
      -e ZINC_FIRST_ADMIN_USER=admin -e ZINC_FIRST_ADMIN_PASSWORD=admin \
      -p 4080:4080 \
      public.ecr.aws/zinclabs/zinc:0.2.5
  echo "$CONTAINER_REPO has been started ..."
fi
