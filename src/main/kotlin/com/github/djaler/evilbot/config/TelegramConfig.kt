package com.github.djaler.evilbot.config

import com.github.insanusmokrassar.TelegramBotAPI.bot.Ktor.KtorRequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.requests.GetMe
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.utils.TelegramAPIUrlsKeeper
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TelegramProperties::class)
class TelegramConfig {
    @Bean
    fun requestsExecutor(telegramProperties: TelegramProperties): RequestsExecutor {
        return KtorRequestsExecutor(TelegramAPIUrlsKeeper(telegramProperties.token))
    }

    @Bean
    fun botInfo(requestsExecutor: RequestsExecutor): User {
        return runBlocking {
            requestsExecutor.execute(GetMe())
        }
    }
}
