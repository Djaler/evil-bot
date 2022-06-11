package com.github.djaler.evilbot

import com.github.djaler.evilbot.config.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties(
    CacheProperties::class,
    TelegramProperties::class,
    BotProperties::class,
    FixerApiProperties::class,
    LocationiqApiProperties::class,
    VKCloudApiProperties::class
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
