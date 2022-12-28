#!/usr/bin/env bash

set -e

# docker build triggers a production install with cargo
docker build -t peregin/velocorner.rates .

# test the image if needed
#docker run --rm -it -p 9012:9012 peregin/velocorner.rates
docker push peregin/velocorner.rates:latest

echo "Successfully deployed..."

