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

## Convert images to webp format
```shell script
for f in *.jpg; do cwebp $f -o "${f%.*}".webp; echo "converted $f file..."; done
```

## Import local database
```shell script
# from host to virtualbox vm
scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i machine/local/.vagrant/machines/velocorner.local/virtualbox/private_key ~/Downloads/velocorner.export.gz vagrant@192.168.0.11:
# on vm
./machine.sh local ssh
# select the proper instance
export DB_ID=$(docker ps -aqf "name=velocorner_database")
export DB_PWD=???
sudo docker cp velocorner.export.gz $DB_ID:/root
sudo docker exec -it $DB_ID console.sh "connect remote:localhost/velocorner root $DB_PWD; import database /root/velocorner.export.gz"
```

# Distribution
```shell script
sbt dist
```
Run with nohup and start as a background process
```shell script
nohup bin/web-app -Dconfig.file=../velocorner.conf &
```

# Release (includes the distribution without manual intervention)
```shell script
sbt "release with-defaults"
```

# Docker
```shell script
sbt docker:publishLocal
docker rm velocorner
docker run -i -d --rm --name velocorner -p 9000:9000 -v /Users/levi/Downloads/velo/velocorner/:/data/ velocorner.com:1.0.3 -Dconfig.file=/data/docker.conf
```

## Papertrail
```shell script
# /etc/rsyslog.d/95-papertrail.conf
# sudo systemctl restart rsyslog.service
if $programname == "systemd" and ($msg contains "Starting Session" or $msg contains "Started Session" or $msg contains "session closed" or $msg contains "session opened" or $msg contains "Created slice" or $msg contains "Starting user" or $msg contains "Starting User Slice of" or $msg contains "Startup" or $msg contains "Removed session" or $msg contains "New session" or $msg contains "Removed slice User Slice of" or $msg contains "Stopping User Slice of" or $msg contains "dwc_otg_handle_mode_mismatch_intr") then stop
if $programname == "systemd-logind" and ($msg contains "Starting Session" or $msg contains "Started Session" or $msg contains "session closed" or $msg contains "session opened" or $msg contains "Created slice" or $msg contains "Starting user-" or $msg contains "Starting User Slice of" or $msg contains "Removed session"  or $msg contains "New session" or $msg contains "Removed slice User Slice of" or $msg contains "Stopping User Slice of") then stop
if $programname == "dhclient" and ($msg contains "DHCP" or $msg contains "renewal") then stop
if $programname == "dockerd" then stop
if $programname == "sshd" then stop
```
