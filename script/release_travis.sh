#!/usr/bin/env bash
if [ -z "$TRAVIS_BRANCH" ]; then
  echo "TRAVIS_BRANCH environment variable must be set"
  exit 1
fi
#git remote rm origin
#git remote add origin https://peregin:${GH_TOKEN}@github.com/peregin/velocorner.com.git > /dev/null 2>&1
#git checkout "$TRAVIS_BRANCH"
git clone https://peregin:${GH_TOKEN}@github.com/peregin/velocorner.com.git > /dev/null 2>&1
git config --global user.name "Deploy CI"
sbt "release skip-tests with-defaults"