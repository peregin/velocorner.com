# IP
AHost to IP address
MX Record velocorner.com
CName www @
CName dev @

# How to run the web application
The main configuration file is not part of the source code, should include the application.conf and the private data, such as Strava API token
```shell script
sbt -Dconfig.file=/Users/levi/Downloads/velo/velocorner/local.conf -Dlog.mode=papertrail 'project web-app' run
```

# Distribution
```shell script
sbt dist
```
run with nohup and start as a background process

nohup bin/web-app -Dconfig.file=../velocorner.conf &

# Release (includes the distribution)
```shell script
sbt "release with-defaults"
```

# Docker
```shell script
sbt docker:publishLocal
docker rm velocorner
docker run -i -d --rm --name velocorner -p 9000:9000 -v /Users/levi/Downloads/velo/velocorner/:/data/ velocorner.com:1.0.3 -Dconfig.file=/data/docker.conf
```
