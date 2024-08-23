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

## Gradle
Useful commands and plugins
```shell
# check for dependency updates
./gradlew checkUpdates
```

## Docker
```shell
docker build -t peregin/velocorner.weather .
docker run --rm -it -p 9015:9015 peregin/velocorner.weather
docker push peregin/velocorner.weather:latest
docker run -it --rm --name weather -p 9015:9015 -v /Users/levi/Downloads/velo/velocorner/:/weather/ -e "config.file=/weather/config/local.conf" peregin/velocorner.weather:latest
```