# How to run the web application
The main configuration file is not part of the source code, should include the application.conf and the private data, such as Strava API token
```shell script
sbt -Dconfig.file=/Users/levi/Downloads/velo/velocorner/local.conf -Dlog.mode=papertrail 'project web-app' run
```

## Convert images to webp format
```shell script
for f in *.jpg; do cwebp $f -o "${f%.*}".webp; echo "converted $f file..."; done
```

## Local database
### OrientDb Import
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
### Psql
Test are using an embedded psql instance `otj-pg-embedded` from opentable.
If for any reason stops working (new macOS) then reinstall the missing libraries with `brew install postgresql`.
#### Import
```shell script
import_psql.sh <filename>
```
#### Export
```shell script
# login to the provisioned machine
backup-psql.sh
```
#### Change Account
```shell script
update account SET data = jsonb_set(data, '{role}', '"admin"'::jsonb) where athlete_id = 432909;
select data->>'role' from account where athlete_id = 432909;
```

# Release (includes the distribution without manual intervention)
```shell script
sbt "release with-defaults"

# or
sbt web-app/assembly
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
if not ($programname contains "velocorner") then stop
*.*          @logs.papertrailapp.com:11477
```

## Images
Free for commercial use
No attribution required

https://pixabay.com/illustrations/christmas-tree-twigs-christmas-pine-1853582/
https://pixabay.com/illustrations/new-year-s-eve-christmas-ornament-1911483/

## HTTP 2
```shell
curl -sI https://velocorner.com -o/dev/null -w '%{http_version}\n'
```

## SBT
Dependency check on demand or Scala Steward will trigger it 
```shell
sbt ";dependencyUpdates; reload plugins; dependencyUpdates"
```
Sbt Task (IntelliJ)
```shell script
sbt 'project web-app' 'run' -Xms512M -Xmx2048M -Xss1M -XX:+CMSClassUnloadingEnabled -Dhttp.port=9001 -Dconfig.file=/Users/levi/Downloads/velo/velocorner/local.conf
```
