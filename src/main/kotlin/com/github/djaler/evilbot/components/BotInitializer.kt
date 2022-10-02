package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.config.TelegramProperties
import com.github.djaler.evilbot.service.CommandService
import com.github.djaler.evilbot.utils.getMD5
import dev.inmo.micro_utils.coroutines.safelyWithoutExceptions
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.setWebhookInfoAndStartListenWebhooks
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import dev.inmo.tgbotapi.requests.webhook.SetWebhook
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class BotInitializer(
    private val requestExecutor: RequestsExecutor,
    private val telegramProperties: TelegramProperties,
    private val updatesManager: UpdatesManager,
    private val sentryClient: SentryClient,
    private val commandService: CommandService,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
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
                        scope.launch {
                            safelyWithoutExceptions({ handleException(it) }) {
                                updatesManager.processUpdate(it)
                            }
                        }
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
                scope.launch {
                    safelyWithoutExceptions({ handleException(it) }) {
                        updatesManager.processUpdate(it)
                    }
                }
            }
        }

        updateCommands()
    }

    private fun updateCommands() {
        runBlocking<Unit> {
            val commandsPerScope = commandService.normalizeCommands(updatesManager.getCommands())
            try {
                commandsPerScope.forEach { (scope, commands) ->
                    requestExecutor.setMyCommands(commands, scope)
                }
            } catch (e: Exception) {
                log.error("Exception on commands set", e)
                sentryClient.captureException(e)
            }
        }
    }

    private fun handleException(throwable: Throwable) {
        log.error("Exception in update parsing", throwable)
        sentryClient.captureException(throwable)
    }
}
