package com.github.djaler.evilbot

import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.config.FixerApiProperties
import com.github.djaler.evilbot.config.LocationiqApiProperties
import com.github.djaler.evilbot.config.TelegramProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.kotlin.coroutine.EnableCoroutine
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableCoroutine
@EnableConfigurationProperties(
    TelegramProperties::class,
    BotProperties::class,
    FixerApiProperties::class,
    LocationiqApiProperties::class
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
