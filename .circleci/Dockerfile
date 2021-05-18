FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-centos-slim

ENV SBT_OPTS="-Dfile.encoding=UTF-8"
ENV SBT_HOME="/opt/sbt"
ENV SBT_VERSION="1.5.2"

RUN mkdir $SBT_HOME
RUN \
  curl -fsL --silent "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" > $SBT_HOME/sbt.tgz && \
  tar zxf $SBT_HOME/sbt.tgz -C $SBT_HOME && \
  ln -s $SBT_HOME/sbt/bin/sbt /usr/local/bin/sbt

RUN yum install -y git

RUN adduser --uid 1500 -c GECOS circleci
USER circleci
WORKDIR /home/circleci/test
RUN sbt sbtVersion

