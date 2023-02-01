# Build image with multiple databases

## Docker
```shell
docker build -t peregin/velocorner.postgres-12 .
docker push peregin/velocorner.postgres-12:latest
```

# Create databases manually
```shell
docker exec -it velo_repo /bin/bash
psql --username velocorner velocorner
create database location;
GRANT ALL PRIVILEGES ON DATABASE location to velocorner;
```
