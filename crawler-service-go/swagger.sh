#!/bin/bash

docker run \
    -d \
    --name crawler-swagger-ui \
    --rm \
    -p 9011:8080 \
    -e SWAGGER_JSON=/src/api.yaml \
    -v "$(pwd)"/api:/src \
    swaggerapi/swagger-ui