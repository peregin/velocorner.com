# CircleCI image with sbt and non root user

CI is running with circleci user - needed for the embedded psql

```shell script
docker build . -t peregin/circleci:latest
# test the image if needed
# docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -u circleci peregin/circleci:latest
docker push peregin/circleci:latest
```