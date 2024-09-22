# Weather Service
Provides 5 days forecast, current weather conditions and suggestions for locations.

## Learn Kotlin
- https://kotlinlang.org/

## Deploy
```shell
./gradlew shadowJar
# x64
docker build -t peregin/velocorner.weather .
docker push peregin/velocorner.weather:latest
# aarch64
docker buildx create --use
docker buildx build --platform linux/amd64,linux/arm64 -t peregin/velocorner.weather:latest --push .
```

## Gradle
Useful commands and plugins
```shell
# check for dependency updates
./gradlew checkUpdates
# upgrade gradle version
./gradlew wrapper --gradle-version 8.10
```

## Docker
```shell
docker run -it --rm --name weather -p 9015:9015 peregin/velocorner.weather:latest
```