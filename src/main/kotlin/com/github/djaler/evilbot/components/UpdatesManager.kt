package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.handlers.UpdateHandler
import io.sentry.SentryClient
import io.sentry.event.Breadcrumb
import io.sentry.event.BreadcrumbBuilder
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
        sentryClient.clearContext()
        for (handler in handlers) {
            try {
                sentryClient.context.recordBreadcrumb(createHandlerBreadcrumb(handler, BreadcrumbCategory.CHECK))
                if (handler.checkUpdate(update)) {
                    sentryClient.context.recordBreadcrumb(createHandlerBreadcrumb(handler, BreadcrumbCategory.HANDLE))
                    val answered = handler.handleUpdate(update)
                    if (answered) {
                        break
                    }

                }
            } catch (e: Exception) {
                log.error("Handler: ${handler::class.simpleName}, update: $update", e)

                sentryClient.context.addExtra("update", update)
                sentryClient.sendException(e)
            }
        }
    }
}

private enum class BreadcrumbCategory {
    CHECK,
    HANDLE
}

private fun createHandlerBreadcrumb(handler: UpdateHandler, category: BreadcrumbCategory): Breadcrumb {
    return BreadcrumbBuilder().apply {
        setMessage(handler::class.java.simpleName)
        setCategory(category.name)
    }.build()
}
