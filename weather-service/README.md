# Weather Service
Provides 5 days forecast, current weather conditions and suggestions for locations.

## Learn Kotlin
- https://kotlinlang.org/

## Deploy
```shell
./gradlew buildFatJar
docker build -t peregin/velocorner.weather .
docker push peregin/velocorner.weather:latest
```

## gradle
Useful commands and plugins
```shell
# check for dependency updates
./gradlew checkUpdates
```

## docker
```shell
docker build -t peregin/velocorner.weather .
docker run --rm -it -p 9015:9015 peregin/velocorner.weather
docker push peregin/velocorner.weather:latest
```