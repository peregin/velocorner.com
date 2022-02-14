#!/usr/bin/env bash

CONTAINER_REPO="elk"
if [[ $(docker inspect -f '{{.State.Running}}' $CONTAINER_REPO) = "true" ]]; then
  echo "$CONTAINER_REPO is already running ..."
else
  echo "starting ELK stack..."
  docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 \
    -it --rm --name $CONTAINER_REPO \
    sebp/elk:7.16.3
  echo "$CONTAINER_REPO has been started ..."
fi
