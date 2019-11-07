#!/usr/bin/env bash

set -e

docker run --rm -p 3000:3000 peregin/web-front:latest
docker push peregin/web-front:latest

