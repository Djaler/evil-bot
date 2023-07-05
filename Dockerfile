# syntax = docker/dockerfile:1.3
FROM openjdk:17.0.2-jdk-slim-bullseye as base

WORKDIR /app

RUN apt-get update && apt-get install -y dos2unix && rm -rf /var/lib/apt/lists/*

COPY gradle ./gradle
COPY ["build.gradle.kts", "gradle.properties",  "gradlew", "settings.gradle", "./"]
RUN dos2unix ./gradlew && chmod +x ./gradlew

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew build || return 0

COPY src/main ./src/main

FROM base as build

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew build

RUN jar xf build/libs/*.jar BOOT-INF META-INF

FROM openjdk:11-jre-slim-bullseye

COPY --from=build /app/BOOT-INF/lib ./lib
COPY --from=build /app/META-INF ./META-INF
COPY --from=build /app/BOOT-INF/classes ./

ENTRYPOINT exec java -cp .:./lib/* com.github.djaler.evilbot.ApplicationKt
