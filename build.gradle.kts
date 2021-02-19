import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.4.30"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
}

group = "com.github.djaler"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/konrad-kaminski/maven")
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.kotlin:spring-kotlin-coroutine:0.3.7")

    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")

    implementation("dev.inmo:tgbotapi:0.32.3")

    implementation("io.ktor:ktor-server-netty:1.5.1")
    implementation("io.ktor:ktor-client-apache:1.5.1")
    implementation("io.ktor:ktor-client-jackson:1.5.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    implementation("io.sentry:sentry-spring-boot-starter:4.1.0")

    implementation("org.unix4j:unix4j-command:0.5")

    implementation("io.github.resilience4j:resilience4j-kotlin:1.7.0")
    implementation("io.github.resilience4j:resilience4j-ratelimiter:1.7.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
