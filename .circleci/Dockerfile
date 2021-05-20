FROM docker:20.10.6-git

LABEL maintainer="velocorner.com@gmail.com"

ENV SHELL "/bin/bash"
ENV LANG="C.UTF-8"
ENV SBT_OPTS="-Dfile.encoding=UTF-8"
ENV SBT_HOME="/opt/sbt"
ENV SBT_VERSION="1.5.2"

RUN apk upgrade --update && \
  apk update --update && \
  apk add --update --no-cache curl bash openjdk11

RUN \
  mkdir $SBT_HOME && \
  curl -fsL --silent "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" > $SBT_HOME/sbt.tgz && \
  tar zxf $SBT_HOME/sbt.tgz -C $SBT_HOME && \
  ln -s $SBT_HOME/sbt/bin/sbt /usr/local/bin/sbt

RUN adduser --uid 1500 -D -g GECOS circleci
#RUN usermod -aG docker circleci
#RUN addgroup circleci docker

USER circleci
WORKDIR /home/circleci/test
RUN sbt sbtVersion

