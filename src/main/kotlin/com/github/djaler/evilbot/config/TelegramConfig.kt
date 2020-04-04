package com.github.djaler.evilbot.config

import com.github.insanusmokrassar.TelegramBotAPI.bot.Ktor.KtorRequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.bot.getMe
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.utils.TelegramAPIUrlsKeeper
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TelegramConfig {
    @Bean
    fun requestsExecutor(telegramProperties: TelegramProperties): RequestsExecutor {
        return KtorRequestsExecutor(TelegramAPIUrlsKeeper(telegramProperties.token))
    }

    @Bean
    fun botInfo(requestsExecutor: RequestsExecutor): ExtendedBot {
        return runBlocking {
            requestsExecutor.getMe()
        }
    }
}
