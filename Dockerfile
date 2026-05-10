# syntax = docker/dockerfile:1.3
FROM eclipse-temurin:17-jdk as build

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

FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    ca-certificates \
    python3 \
    python3-pip \
 && python3 -m pip install --no-cache-dir --break-system-packages yt-dlp \
 && rm -rf /root/.cache/pip \
 && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/dependencies/ ./
COPY --from=build /app/spring-boot-loader/ ./
COPY --from=build /app/snapshot-dependencies/ ./
COPY --from=build /app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
