# syntax = docker/dockerfile:1.3
FROM openjdk:11-jdk-slim-bullseye as build

WORKDIR /app

RUN apt-get update && apt-get install -y dos2unix && rm -rf /var/lib/apt/lists/*

COPY gradle ./gradle
COPY ["build.gradle.kts", "gradle.properties",  "gradlew", "settings.gradle", "./"]
RUN dos2unix ./gradlew && chmod +x ./gradlew

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew build || return 0

COPY src/main ./src/main

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew build
RUN java -Djarmode=layertools -jar build/libs/evil-bot-1.0.jar extract

FROM openjdk:11-jre-slim-bullseye

COPY --from=build /app/dependencies/ ./
COPY --from=build /app/spring-boot-loader/ ./
COPY --from=build /app/snapshot-dependencies/ ./
COPY --from=build /app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
