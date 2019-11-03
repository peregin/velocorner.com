#!/usr/bin/env bash

set -e

if [ -z "$TRAVIS_BRANCH" ]; then
  echo "TRAVIS_BRANCH environment variable must be set"
  exit 1
fi
echo "commit message is [$TRAVIS_COMMIT_MESSAGE]"

docker login -u "$DOCKERHUB_USER" -p "$DOKERHUB_TOKEN"

git checkout "$TRAVIS_BRANCH"
git config --global user.name "Deploy CI"
sbt "release skip-tests with-defaults"
git push --quiet https://peregin:${GH_TOKEN}@github.com/peregin/velocorner.com.git "$TRAVIS_BRANCH"