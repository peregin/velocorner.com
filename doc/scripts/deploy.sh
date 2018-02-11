#!/usr/bin/env bash

if [ $# -eq 0 ]; then
    echo "Provide the file name to be deployed"
    exit 1
fi

SOURCEFILE=$1
echo "deploying $SOURCEFILE"
DIRNAME=`basename $SOURCEFILE`
DEPLOYDIR=${DIRNAME%.zip}
echo "target directory is $DEPLOYDIR"


./stopapp.sh
rm -rf velocorner/
cp $SOURCEFILE ./web-app.zip
unzip web-app.zip
rm web-app.zip
mv $DEPLOYDIR velocorner
./startapp.sh