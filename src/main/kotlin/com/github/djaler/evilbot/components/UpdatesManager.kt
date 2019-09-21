package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.handlers.UpdateHandler
import io.sentry.SentryClient
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class UpdatesManager(
    handlers: List<UpdateHandler>,
    private val sentryClient: SentryClient
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    private val handlers = handlers.sortedBy { it.order }

    fun processUpdate(update: Update) {
        for (handler in handlers) {
            sentryClient.clearContext()

            try {
                if (handler.checkUpdate(update)) {
                    val answered = handler.handleUpdate(update)
                    if (answered) {
                        break
                    }

                }
            } catch (e: Exception) {
                log.error("Handler: ${handler::class.simpleName}, update: $update", e)

                sentryClient.context.addExtra("handler", handler::class.simpleName)
                sentryClient.context.addExtra("update", update)
                sentryClient.sendException(e)
            }
        }
    }
}
