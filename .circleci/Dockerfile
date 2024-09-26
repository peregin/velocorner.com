# it is a Debian distribution (Debian GNU/Linux 10 (buster) with java 17, sbt 1.5.0, node 16, docker
FROM circleci/openjdk:17-buster-node-browsers

LABEL maintainer="velocorner.com@gmail.com"

ENV SHELL="/bin/bash"
ENV LANG="C.UTF-8"
ENV SBT_OPTS="-Dfile.encoding=UTF-8"
ENV SBT_HOME="/opt/sbt"
ENV SBT_VERSION="1.10.2"

USER root
# install sbt (remove old 1.5)
RUN rm -rf $SBT_HOME && \
  mkdir $SBT_HOME
RUN \
  curl -fsL --silent "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" > $SBT_HOME/sbt.tgz && \
  tar zxf $SBT_HOME/sbt.tgz -C $SBT_HOME && \
  ln -s $SBT_HOME/sbt/bin/sbt /usr/local/bin/sbt

# for multiplatform docker build
RUN apt-key adv --recv-keys --keyserver keyserver.ubuntu.com E88979FB9B30ACF2 && \
  apt-get update && \
  apt-get install -y qemu-user-static && \
  apt-get install -y binfmt-support

USER circleci
WORKDIR /home/circleci/test
RUN sbt sbtVersion

