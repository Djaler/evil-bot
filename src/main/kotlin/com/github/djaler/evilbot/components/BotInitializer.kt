package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.config.TelegramProperties
import com.github.djaler.evilbot.utils.getMD5
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.bot.getMyCommands
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.setWebhookInfoAndStartListenWebhooks
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import dev.inmo.tgbotapi.requests.webhook.SetWebhook
import io.ktor.server.netty.*
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
                    sentryClient.captureException(e)
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

        updateCommandsIfNeeded()
    }

    private fun updateCommandsIfNeeded() {
        runBlocking<Unit> {
            try {
                val oldCommands = requestExecutor.getMyCommands()
                val newCommands = updatesManager.getCommands()
                if (oldCommands != newCommands) {
                    requestExecutor.setMyCommands(newCommands)
                }
            } catch (e: Exception) {
                log.error("Exception on commands set", e)
                sentryClient.captureException(e)
            }
        }
    }

    private suspend fun handleException(throwable: Throwable) {
        log.error("Exception in update parsing", throwable)
        sentryClient.captureException(throwable)
    }
}
