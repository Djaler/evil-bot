package com.github.djaler.evilbot

import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.config.TelegramProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TelegramProperties::class, BotProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
