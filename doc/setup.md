# git on nas
git init --bare velocorner.git
git clone ssh://nas/volume1/gitrepo/velocorner.git

# IP
AHost to IP address
MX Record velocorner.com
CName www @
CName dev @

# Analytics 
tracking id: `UA-573257-3`

# Server
mkdir /var/www/velocorner.com
add a plain static page - coming soon page
nohup ./web-app &

# Apache
add VirtualHost in httpd.conf
exclude proxy for static content
server alias for www
websocket setup
LoadModule proxy_wstunnel_module /usr/lib64/httpd/modules/mod_proxy_wstunnel.so
ProxyPass /ws ws://localhost:9000/ws retry=4
ProxyPassReverse /ws ws://localhost:9000/ws retry=4

# Email
makemap hash /etc/mail/virtusertable < /etc/mail/virtusertable

# How to run the web application
The main configuration file is not part of the source code, should include the application.conf and the private data, such as Strava API token
```shell script
sbt -Dconfig.file=/Users/levi/Downloads/velo/velocorner/local.conf 'project web-app' run
```

# Distribution
sbt dist
run with nohup and start as a background process

nohup bin/web-app -Dconfig.file=../velocorner.conf &

# Release (includes the distribution)
```shell script
sbt release
```
// won't request acknowledgment for release version and push changes
```
sbt 'release with-defaults'
```
# Docker
sbt docker:publishLocal
docker rm velocorner
docker run -i -d --rm --name velocorner -p 9000:9000 -v /Users/levi/Downloads/velo/velocorner/:/data/ velocorner.com:1.0.3-SNAPSHOT -Dconfig.file=/data/docker.conf
