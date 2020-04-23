package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.config.TelegramProperties
import com.github.djaler.evilbot.utils.getMD5
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.updates.startGettingOfUpdates
import com.github.insanusmokrassar.TelegramBotAPI.utils.extensions.setWebhook
import io.ktor.server.netty.Netty
import io.sentry.SentryClient
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class BotInitializer(
    private val requestExecutor: RequestsExecutor,
    private val telegramProperties: TelegramProperties,
    private val updatesManager: UpdatesManager,
    private val sentryClient: SentryClient
) {
    companion object {
        private val log = LogManager.getLogger()
    }

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
                    engineFactory = Netty,
                    allowedUpdates = updatesManager.getAllowedUpdates(),
                    exceptionsHandler = { handleException(it) }
                ) {
                    updatesManager.processUpdate(it)
                }
            }
        } else {
            requestExecutor.startGettingOfUpdates(
                allowedUpdates = updatesManager.getAllowedUpdates(),
                exceptionsHandler = { handleException(it) }
            ) {
                updatesManager.processUpdate(it)
            }
        }
    }

    private suspend fun handleException(exception: Exception) {
        log.error("Exception in update parsing", exception)
        sentryClient.sendException(exception)
    }
}
