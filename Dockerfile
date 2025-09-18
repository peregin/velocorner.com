FROM eclipse-temurin:21-jre-alpine
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75" TZ=UTC
RUN addgroup -S app && adduser -S app -G app

RUN mkdir /app
COPY web-app/target/scala-2.13/web-app-all.jar /app/web-app-all.jar
WORKDIR /app

EXPOSE 9000
USER app
ENTRYPOINT ["java", "-Dlog.mode=papertrail", "-Dplay.server.pidfile.path=/dev/null", "-Duser.timezone=UTC", "-jar", "web-app-all.jar"]
CMD []
