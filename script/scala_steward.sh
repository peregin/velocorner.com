#!/usr/bin/env bash

docker run -v /Users/levi/dev/velocorner.com/script/scala-steward:/opt/scala-steward -it fthomas/scala-steward:latest \
  --workspace  "/opt/scala-steward/workspace" \
  --repos-file "/opt/scala-steward/repos.md" \
  --git-author-email velocorner.com@gmail.com \
  --vcs-api-host "https://api.github.com" \
  --vcs-login peregin \
  --git-ask-pass "/opt/scala-steward/token.sh" \
  --do-not-fork