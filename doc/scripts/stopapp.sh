#!/usr/bin/env bash

# stop file for running the app
PID_FILE=velocorner/RUNNING_PID
if [ -f $PID_FILE ]; then
    kill -9 `cat $PID_FILE`
    rm $PID_FILE
else
    echo "PID file $PID_FILE is missing"
fi