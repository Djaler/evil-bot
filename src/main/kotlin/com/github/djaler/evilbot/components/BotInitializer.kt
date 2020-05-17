package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.config.TelegramProperties
import com.github.djaler.evilbot.utils.getMD5
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.updates.retrieving.setWebhookInfoAndStartListenWebhooks
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import com.github.insanusmokrassar.TelegramBotAPI.requests.webhook.SetWebhook
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
                try {
                    requestExecutor.setWebhookInfoAndStartListenWebhooks(
                        setWebhookRequest = SetWebhook(
                            url = webhook.url + path,
                            allowedUpdates = updatesManager.getAllowedUpdates()
                        ),
                        engineFactory = Netty,
                        listenHost = "0.0.0.0",
                        listenPort = webhook.port,
                        listenRoute = path,
                        exceptionsHandler = { handleException(it) }
                    ) {
                        updatesManager.processUpdate(it)
                    }
                } catch (e: Exception) {
                    log.error("Exception on webhook setup", e)
                    sentryClient.sendException(e)
                }
            }
        } else {
            requestExecutor.startGettingOfUpdatesByLongPolling(
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
