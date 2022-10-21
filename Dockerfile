# syntax = docker/dockerfile:1.3
FROM openjdk:11-jdk-slim-bullseye as base

WORKDIR /app

COPY gradle ./gradle
COPY ["build.gradle.kts", "gradle.properties",  "gradlew", "settings.gradle", "./"]

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
