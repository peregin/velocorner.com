FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM openjdk:17-slim-buster
EXPOSE 9015:9015
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/service.jar /app/weather-service.jar
ENTRYPOINT ["java","-Duser.timezone=UTC","-jar","/app/weather-service.jar"]
CMD []