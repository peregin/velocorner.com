# CircleCI image with sbt and non-root user

CI is running with circleci user - needed for the embedded psql

```shell script
# test the image if needed
docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -u circleci peregin/circleci:latest /bin/bash

# x86
docker build . -t peregin/circleci:latest
docker push peregin/circleci:latest

# arm
buildx inspect multi-arch-builder
buildx create --name multi-arch-builder
docker buildx use multi-arch-builder
docker buildx build --platform linux/amd64,linux/arm64 -t peregin/circleci:latest --push .
```

Cleanup old docker images in a batch
```shell
docker image prune
docker rmi $(docker images --format '{{.Repository}}:{{.Tag}} {{.ID}}' | grep 'velo' | cut -d' ' -f2)
```