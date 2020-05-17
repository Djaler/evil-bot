package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.config.TelegramProperties
import com.github.djaler.evilbot.utils.getMD5
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.updates.retrieving.includeWebhookHandlingInRoute
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import com.github.insanusmokrassar.TelegramBotAPI.requests.webhook.SetWebhook
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import com.github.insanusmokrassar.TelegramBotAPI.updateshandlers.UpdateReceiver
import com.github.insanusmokrassar.TelegramBotAPI.utils.ExceptionHandler
import io.ktor.routing.createRouteFromPath
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.sentry.SentryClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
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
                    requestExecutor.execute(
                        SetWebhook(
                            url = webhook.url + path,
                            allowedUpdates = updatesManager.getAllowedUpdates()
                        )
                    )

                    val listenRoute = path
                    val listenHost = "0.0.0.0"
                    val listenPort = webhook.port
                    val scope = CoroutineScope(Executors.newFixedThreadPool(4).asCoroutineDispatcher())
                    val exceptionsHandler: ExceptionHandler<Unit> = { handleException(it) }
                    val block: UpdateReceiver<Update> = {
                        updatesManager.processUpdate(it)
                    }

                    val env = applicationEngineEnvironment {
                        module {
                            routing {
                                listenRoute.also {
                                    createRouteFromPath(it).includeWebhookHandlingInRoute(
                                        scope,
                                        exceptionsHandler,
                                        block
                                    )
                                }
                            }
                        }
                        connector {
                            host = listenHost
                            port = listenPort
                        }
                    }
                    embeddedServer(Netty, env).start(false)

                    /*requestExecutor.setWebhookInfoAndStartListenWebhooks(
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
                    }*/
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
