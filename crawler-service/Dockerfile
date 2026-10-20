# ---- Stage 1: Build a minimal custom JRE
FROM eclipse-temurin:21-jdk-jammy AS jre-builder

WORKDIR /work
COPY target/scala-3.9.0/crawler-service-all.jar /work/app.jar

RUN jdeps \
      --multi-release 21 \
      --ignore-missing-deps \
      --print-module-deps /work/app.jar > /work/deps.txt && \
    "$JAVA_HOME/bin/jlink" \
      --add-modules "$(cat /work/deps.txt)",jdk.crypto.ec,jdk.unsupported \
      --strip-debug \
      --no-header-files \
      --no-man-pages \
      --compress=2 \
      --output /customjre && \
    /customjre/bin/java -version

# ---- Stage 2: Install only the Playwright JS dependency
FROM node:22-bookworm-slim AS playwright-deps

WORKDIR /work/playwright
COPY playwright/package.json playwright/package-lock.json ./

RUN npm ci --omit=dev --no-audit --no-fund && \
    npm cache clean --force

# ---- Stage 3: Slim runtime with Chromium, custom JRE, and copied Node binary
FROM debian:bookworm-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends chromium ca-certificates nodejs && \
    rm -rf /var/lib/apt/lists/* && \
    useradd --system --create-home --uid 10001 appuser

WORKDIR /app

COPY --from=jre-builder /customjre /jre
COPY --from=playwright-deps /work/playwright/node_modules /app/crawler-service/playwright/node_modules
COPY target/scala-3.9.0/crawler-service-all.jar /app/crawler-service-all.jar
COPY playwright/package.json /app/crawler-service/playwright/package.json
COPY playwright/bike24-fetch.mjs /app/crawler-service/playwright/bike24-fetch.mjs
COPY playwright/bike-discount-fetch.mjs /app/crawler-service/playwright/bike-discount-fetch.mjs

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75" \
    TZ=UTC \
    NODE_ENV=production \
    CHROME_EXECUTABLE_PATH=/usr/bin/chromium \
    PATH="/jre/bin:${PATH}"

USER appuser

EXPOSE 9011

ENTRYPOINT ["/jre/bin/java", "-Dlog.mode=grafana", "-Duser.timezone=UTC", "-jar", "/app/crawler-service-all.jar"]
