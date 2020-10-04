package com.github.djaler.evilbot.config

import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.bot.getMe
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.telegramBot
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
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
