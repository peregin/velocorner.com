[![CircleCI](https://img.shields.io/circleci/build/github/peregin/velocorner.com/master?token=10fafb0dd1fbf4349da8c133d0a0ec3e64d74cfe)](https://app.circleci.com/pipelines/github/peregin/velocorner.com)
[![Maintainability](https://api.codeclimate.com/v1/badges/fb859d66691e27cb4295/maintainability)](https://codeclimate.com/github/peregin/velocorner.com/maintainability)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Issues](https://img.shields.io/github/issues/peregin/velocorner.com.svg)](https://github.com/peregin/velocorner.com/issues)
[![Swagger Validator](https://img.shields.io/swagger/valid/2.0/https/raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json.svg)](http://velocorner.com/docs)
[![Docker Pulls](https://img.shields.io/docker/pulls/peregin/velocorner.com)](https://hub.docker.com/r/peregin/velocorner.com)
[![StackShare](http://img.shields.io/badge/tech-stack-0690fa.svg?style=flat)](https://stackshare.io/velocorner/velocorner)

# velocorner.com
The web application provides metrics for cycling and running (or for other kind of sports) activities.
The statistics page compares yearly aggregated data, such as distance, hours ridden, elevation gained and shows year to 
date series as well.
The yearly data is presented as a heatmap, so it is easy to compare the efforts for a given period of the year.
The data feed is collected via the [Strava API](https://developers.strava.com/docs/reference/).

![logo](https://raw.github.com/peregin/velocorner.com/master/doc/graphics/logo50.png "logo")
Visit the page at [http://velocorner.com](http://velocorner.com), I'd love to hear your feedback!
Also using this project to experiment with technologies.

## CI/CD Flow

![CI/CD](https://raw.github.com/peregin/velocorner.com/master/doc/graphics/cicd.png "CI/CD")

## Infrastructure
Follows the infrastructure as code approach, see more details in this [repo](https://github.com/peregin/my-little-infra "Infrastructure") .

![Infrastructure](https://raw.github.com/peregin/my-little-infra/master/doc/infra.png "Infrastructure")

## Local Setup

### Mirror Infrastructure
Start local infrastructure and deploy the stack

### Individual Services
Start database
```shell script
# start and import database
./script/start_psql.sh
./script/import_psql.sh ~/Downloads/velo/velocorner/backup/psql-202008031826.sql.gz

# start web application
sbt -Xms512M \
    -Xmx2048M \
    -Xss1M \
    -XX:+CMSClassUnloadingEnabled \
    -Dhttp.port=9001 \
    -Dconfig.file=/Users/levi/Downloads/velo/velocorner/local.conf \
  "project web-app" run
```