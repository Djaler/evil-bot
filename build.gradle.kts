import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.3.41"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "2.1.9.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
}

group = "com.github.djaler"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.telegram:telegrambots-spring-boot-starter:4.4.0.1")

    implementation("io.sentry:sentry:1.7.28")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
