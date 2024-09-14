FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:17-jdk-alpine
EXPOSE 9015:9015
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/service.jar /app/service.jar
ENTRYPOINT ["java","-jar","/app/service.jar"]