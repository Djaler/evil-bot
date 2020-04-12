package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.handlers.UpdateHandler
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import io.sentry.SentryClient
import io.sentry.event.Breadcrumb
import io.sentry.event.BreadcrumbBuilder
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component

@Component
class UpdatesManager(
    handlers: List<UpdateHandler>,
    private val sentryClient: SentryClient
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    private val handlers = handlers.sortedBy { it.order }

    fun getAllowedUpdates(): List<String> {
        return handlers.map { it.updateType }.distinct()
    }

    suspend fun processUpdate(update: Update) {
        sentryClient.clearContext()
        for (handler in handlers) {
            try {
                sentryClient.context.recordBreadcrumb(createHandlerBreadcrumb(handler))
                val answered = handler.handleUpdate(update)
                if (answered) {
                    break
                }
            } catch (e: Exception) {
                log.error("Handler: ${handler::class.simpleName}, update: $update", e)

                sentryClient.context.addExtra("update", update)
                sentryClient.sendException(e)
            }
        }
    }
}

private fun createHandlerBreadcrumb(handler: UpdateHandler): Breadcrumb {
    return BreadcrumbBuilder().apply {
        setMessage(handler::class.java.simpleName)
    }.build()
}
