package com.github.djaler.evilbot.config

import com.github.djaler.evilbot.components.UpdatesManager
import com.github.djaler.evilbot.utils.getMD5
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.bots.DefaultAbsSender
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.LongPollingBot
import org.telegram.telegrambots.meta.generics.WebhookBot
import org.telegram.telegrambots.util.WebhookUtils

@Configuration
@EnableConfigurationProperties(TelegramProperties::class)
class TelegramConfig(
    private val telegramProperties: TelegramProperties
) {
    @Bean
    fun botOptions(): DefaultBotOptions {
        return ApiContext.getInstance(DefaultBotOptions::class.java)
    }

    @Bean
    fun sender(
        defaultBotOptions: DefaultBotOptions,
        telegramProperties: TelegramProperties
    ): DefaultAbsSender {
        return object : DefaultAbsSender(defaultBotOptions) {
            override fun getBotToken() = telegramProperties.token
        }
    }

    @Bean
    fun botUsername(sender: DefaultAbsSender): String {
        return sender.me.userName
    }

    @Bean
    fun telegramBotsApi(): TelegramBotsApi {
        val webhook = telegramProperties.webhook

        return if (webhook.url != null) {
            TelegramBotsApi(webhook.url, "http://0.0.0.0:${webhook.port}")
        } else {
            TelegramBotsApi()
        }
    }

    @Bean
    fun bot(
        sender: DefaultAbsSender,
        updatesManager: UpdatesManager,
        botOptions: DefaultBotOptions,
        botUsername: String
    ): Any {
        val webhook = telegramProperties.webhook

        if (webhook.url != null) {
            return object : WebhookBot {
                override fun getBotToken() = telegramProperties.token

                override fun getBotPath() = telegramProperties.token.getMD5()

                override fun getBotUsername() = botUsername

                override fun setWebhook(url: String, publicCertificatePath: String?) {
                    WebhookUtils.setWebhook(sender, url, publicCertificatePath)
                }

                override fun onWebhookUpdateReceived(update: Update): BotApiMethod<*>? {
                    updatesManager.processUpdate(update)
                    return null
                }

            }
        } else {
            return object : LongPollingBot {
                override fun getBotToken() = telegramProperties.token

                override fun onUpdateReceived(update: Update) {
                    updatesManager.processUpdate(update)
                }

                override fun getBotUsername() = botUsername

                override fun getOptions() = botOptions

                override fun clearWebhook() {
                    WebhookUtils.clearWebhook(sender)
                }
            }
        }
    }
}
