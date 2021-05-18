# CircleCI image with sbt and non root user

```shell script
docker build . -t peregin/circleci:latest
# test the image if needed
# docker run --rm -it -u circleci peregin/circleci:latest /bin/sh
docker push peregin/circleci:latest
```