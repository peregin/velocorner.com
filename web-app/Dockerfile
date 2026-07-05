# ---- Stage 1: Build a minimal custom JRE on a glibc image
FROM eclipse-temurin:21-jdk-jammy AS jre-builder

WORKDIR /work
COPY target/scala-3.8.2/web-app-all.jar /work/app.jar

# Inspect module deps, then build a trimmed JRE.
# Add jdk.crypto.ec for TLS and jdk.unsupported for sun.misc.Unsafe (often needed by Scala/Play).
RUN jdeps \
      --multi-release 21 \
      --ignore-missing-deps \
      --print-module-deps /work/app.jar > /work/deps.txt && \
    $JAVA_HOME/bin/jlink \
      --add-modules $(cat /work/deps.txt),jdk.crypto.ec,jdk.unsupported \
      --strip-debug \
      --no-header-files \
      --no-man-pages \
      --compress=2 \
      --output /customjre && \
    /customjre/bin/java -version

# ---- Stage 2: Extract wget with all shared library dependencies
FROM debian:bookworm-slim AS wget-builder

RUN apt-get update && \
    apt-get install --no-install-recommends -y wget && \
    apt-get clean && rm -rf /var/lib/apt/lists/* && \
    mkdir -p /wget-root && \
    cp -v /usr/bin/wget /wget-root/ && \
    for lib in $(ldd /usr/bin/wget | grep -o '/[^ ]*' | sort -u); do \
        mkdir -p "/wget-root$(dirname $lib)" && \
        cp -vn "$lib" "/wget-root$lib"; \
    done

# ---- Stage 3: Ultra-small runtime (glibc)
FROM gcr.io/distroless/base-debian12

# Use the built-in nonroot user (UID/GID 65532)
USER 65532:65532

WORKDIR /app
COPY --from=jre-builder /customjre /jre
COPY --from=wget-builder /wget-root/ /
COPY target/scala-3.8.2/web-app-all.jar /app/web-app-all.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75" \
    TZ=UTC \
    PATH="/jre/bin:${PATH}"

EXPOSE 9000

# Health check (exec form — Distroless has no shell)
HEALTHCHECK --interval=60s --timeout=3s --start-period=5s --retries=3 \
    CMD ["wget", "--no-verbose", "--tries=1", "--spider", "http://127.0.0.1:9000/api/ping"]

# Use absolute path (no shell in Distroless; PATH lookup isn’t used by Docker exec)
ENTRYPOINT ["/jre/bin/java", "-Dlog.mode=grafana", "-Dplay.server.pidfile.path=/dev/null", "-Duser.timezone=UTC", "-jar", "/app/web-app-all.jar"]
