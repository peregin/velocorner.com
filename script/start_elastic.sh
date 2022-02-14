#!/usr/bin/env bash

CONTAINER_REPO="elasticsearch"
if [[ $(docker inspect -f '{{.State.Running}}' $CONTAINER_REPO) = "true" ]]; then
  echo "$CONTAINER_REPO is already running ..."
else
  echo "starting $CONTAINER_REPO ..."
  docker run -d --rm --name $CONTAINER_REPO -p 9200:9200 -p 9300:9300 \
    -e "xpack.security.enabled=false" \
    -e "discovery.type=single-node" \
    -e "ES_JAVA_OPTS=-Xms256m -Xmx750m" \
    -v $HOME/Downloads/velo/velocorner/elasticsearch/data:/usr/share/elasticsearch/data \
    elasticsearch:7.17.0
  echo "$CONTAINER_REPO has been started ..."
fi
