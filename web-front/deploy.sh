#!/usr/bin/env bash

set -e

npm version patch
CURRENT_VERSION=$(npm run version --silent)
echo "Current version is $CURRENT_VERSION"

# docker build triggers a production install with npm
docker build . -t peregin/web-front:latest

# test the image if needed
# docker run --rm -p 3000:3000 peregin/web-front:latest
docker push peregin/web-front:latest

echo "Successfully deployed..."

