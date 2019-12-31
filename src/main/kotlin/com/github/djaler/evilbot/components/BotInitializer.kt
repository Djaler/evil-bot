package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.config.TelegramProperties
import com.github.djaler.evilbot.utils.getMD5
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.utils.extensions.setWebhook
import com.github.insanusmokrassar.TelegramBotAPI.utils.extensions.startGettingOfUpdates
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class BotInitializer(
    private val requestExecutor: RequestsExecutor,
    private val telegramProperties: TelegramProperties,
    private val updatesManager: UpdatesManager
) {
    @PostConstruct
    fun init() {
        val webhook = telegramProperties.webhook
        if (webhook.url !== null) {
            val path = telegramProperties.token.getMD5()

            runBlocking {
                requestExecutor.setWebhook(
                    url = webhook.url + path,
                    listenHost = "0.0.0.0",
                    listenRoute = path,
                    port = webhook.port,
                    engineFactory = Netty
                ) {
                    updatesManager.processUpdate(it)
                }
            }
        } else {
            requestExecutor.startGettingOfUpdates(timeoutMillis = 1000) {
                updatesManager.processUpdate(it)
            }
        }
    }
}
