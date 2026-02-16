FROM cimg/openjdk:17.0.11

LABEL maintainer="velocorner.com@gmail.com"

ENV SHELL="/bin/bash"
ENV LANG="C.UTF-8"
ENV SBT_OPTS="-Dfile.encoding=UTF-8"
ENV SBT_HOME="/opt/sbt"
ENV SBT_VERSION="1.12.3"

USER root
# install sbt
RUN rm -rf $SBT_HOME && \
  mkdir $SBT_HOME
RUN \
  curl -fsL --silent "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" > $SBT_HOME/sbt.tgz && \
  tar zxf $SBT_HOME/sbt.tgz -C $SBT_HOME && \
  ln -s $SBT_HOME/sbt/bin/sbt /usr/local/bin/sbt

USER circleci
WORKDIR /home/circleci/test
RUN sbt --version

