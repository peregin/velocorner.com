#!/usr/bin/env bash

set -e

npm version patch
CURRENT_VERSION=$(npm run version --silent)
echo "Current version is $CURRENT_VERSION"
#git commit -a -m 'Setting version to $CURRENT_VERSION'
npm run clean
npm install
npm run build

docker build . -t peregin/web-front:latest
# test the image
# docker run --rm -p 3000:3000 peregin/web-front:latest
docker push peregin/web-front:latest

#git push

echo "Successfully deployed..."

