#!/usr/bin/env bash

function printUsage() {
  echo $"Usage: $(basename "$0") <file>"
}

DB_FILE=$1
if [ -z "$DB_FILE" ]; then
  printUsage
  exit 1
fi

echo "Database import from $1"

CONTAINER_REPO="velo_repo"
DB_ID=$(docker ps -aqf "name=$CONTAINER_REPO")
echo "PSQL container id $DB_ID"
docker cp $DB_FILE $DB_ID:psql.export.gz
echo "file $DB_FILE has been copied"
docker exec -it $DB_ID bash -c "gunzip < psql.export.gz | psql -U velocorner -d velocorner"
echo "database has been imported"

