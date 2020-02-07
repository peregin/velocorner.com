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

git commit -a -m "Setting web-front version to $CURRENT_VERSION"
TAG_TEXT="web-front-v$CURRENT_VERSION"
git tag -a $TAG_TEXT -m "$TAG_TEXT"

echo "Successfully deployed..."

