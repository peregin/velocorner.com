#!/usr/bin/env bash

set -e

# docker build triggers a production install with cargo
docker build -t peregin/velocorner.weather .

# test the image if needed
#docker run -it --rm --name weather -p 9015:9015 -v /Users/levi/Downloads/velo/velocorner/:/weather/ -e "config.file=/weather/local.conf" peregin/velocorner.weather:latest
docker push peregin/velocorner.weather:latest

echo "Successfully deployed..."

