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
### Psql
Test are using an embedded psql instance `otj-pg-embedded` from opentable.
If for any reason stops working (new macOS) then reinstall the missing libraries with `brew install postgresql`.
```shell script
# read the data
import_psql.sh <filename>
# login to the provisioned machine
backup-psql.sh
```

#### Change Account
```shell script
update account SET data = jsonb_set(data, '{role}', '"admin"'::jsonb) where athlete_id = 432909;
select data->>'role' from account where athlete_id = 432909;
```

# Release
```shell script
sbt web-app/assembly
```

# Docker
```shell script
deploy.sh
docker run --env-file local.env -e DB_URL=jdbc:postgresql://host.docker.internal:5492/velocorner -p 9000:9000 peregin/velocorner.com:latest
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
sbt 'project web-app' 'run' -Xms512M -Xmx2048M -Xss1M -XX:+CMSClassUnloadingEnabled -Dhttp.port=9001
```
