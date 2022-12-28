#!/usr/bin/env bash

CONTAINER_REPO="rates"
if [[ $(docker inspect -f '{{.State.Running}}' $CONTAINER_REPO) = "true" ]]; then
  echo "$CONTAINER_REPO is already running ..."
else
  echo "starting $CONTAINER_REPO ..."
  docker run -d --rm --name $CONTAINER_REPO \
      -p 9012:9012 \
      peregin/velocorner.rates
  echo "$CONTAINER_REPO has been started ..."
fi
