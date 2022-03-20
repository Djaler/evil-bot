# syntax = docker/dockerfile:1.3
FROM gradle:jdk11 as builder
RUN mkdir -p out
WORKDIR /home/gradle
COPY --chown=gradle . /home/gradle/
RUN --mount=type=cache,target=/home/gradle/myapp/.gradle \
    --mount=type=cache,target=/home/gradle/myapp/build \
    gradle bootJar --no-daemon && cp build/libs/*.jar /home/gradle/out

FROM openjdk:11-jre as bin-linux
COPY --from=builder /home/gradle/out/evil-bot-1.0-SNAPSHOT.jar /usr/bin/evil-bot.jar
RUN adduser --system app
USER app
WORKDIR /home/app
CMD ["java", "-jar", "/usr/bin/evil-bot.jar"]
