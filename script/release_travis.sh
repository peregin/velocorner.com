#!/usr/bin/env bash

set -e

if [ -z "$TRAVIS_BRANCH" ]; then
  echo "TRAVIS_BRANCH environment variable must be set"
  exit 1
fi
echo "commit message is [$TRAVIS_COMMIT_MESSAGE]"

# login to docker.io, sbt release will push the image
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin

git checkout "$TRAVIS_BRANCH"
git config --global user.name "Deploy CI"

# build and push web-app
# bumps version, commit the changes and tags the code base
echo "deploying web-app"
sbt "release skip-tests with-defaults"

# build and push web-front
# bump version, commit changes and tags the code base
echo "deploying web-front"
cd web-front
sh deploy.sh
cd ..

# push version changes and tags to the github
git push --tags --quiet https://peregin:"${GH_TOKEN}"@github.com/peregin/velocorner.com.git "$TRAVIS_BRANCH"

# restart the docker swarm stack
ssh -i script/deploy_key -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null "$DEPLOYER_USER"@velocorner.com '/opt/velocorner/deploy.sh'