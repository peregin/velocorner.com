#!/usr/bin/env bash

# start file for running the app

cd velocorner
nohup bin/web-app -Dconfig.file=../velocorner.conf &
cd ..
