package com.github.djaler.evilbot.config

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.types.ExtendedBot
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TelegramConfig {
    @Bean
    fun requestsExecutor(telegramProperties: TelegramProperties): RequestsExecutor {
        return telegramBot(telegramProperties.token)
    }

    @Bean
    fun botInfo(requestsExecutor: RequestsExecutor): ExtendedBot {
        return runBlocking {
            requestsExecutor.getMe()
        }
    }
}
