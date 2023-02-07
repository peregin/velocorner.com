#!/usr/bin/env bash

# initializes additional databases next to the original one (velocorner)
declare -a modules
modules=(location weather user)

for module in "${modules[@]}"; do
    echo "creating databases for $module"
    psql --username "$POSTGRES_USER" "$POSTGRES_DB" <<EOF
CREATE DATABASE ${module};
GRANT ALL PRIVILEGES ON DATABASE ${module} to velocorner;
EOF
done
