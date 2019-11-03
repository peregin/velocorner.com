#!/usr/bin/env bash

set -e

if [ -z "$TRAVIS_BRANCH" ]; then
  echo "TRAVIS_BRANCH environment variable must be set"
  exit 1
fi
echo "commit message is [$TRAVIS_COMMIT_MESSAGE]"

echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin

git checkout "$TRAVIS_BRANCH"
git config --global user.name "Deploy CI"
sbt "release skip-tests with-defaults"
# push version changes and tags to the github
git push --quiet https://peregin:${GH_TOKEN}@github.com/peregin/velocorner.com.git "$TRAVIS_BRANCH"

# restart the docker swarm stack
sshpass -p "$DEPLOYER_TOKEN" ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null "$DEPLOYER_USER"@velocorner.com '/opt/velocorner/deploy.sh'