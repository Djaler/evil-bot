package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.handlers.base.UpdateHandler
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.update.abstracts.UnknownUpdate
import dev.inmo.tgbotapi.types.update.abstracts.Update
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component

@Component
class UpdatesManager(
    handlers: List<UpdateHandler>,
    private val exceptionsManager: ExceptionsManager,
    private val sentryClient: SentryClient
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    private val handlers = handlers.sortedBy { it.order }

    fun getAllowedUpdates(): List<String> {
        return handlers.map { it.updateType }.distinct()
    }

    fun getCommands(): Map<BotCommandScope, List<BotCommand>> {
        return handlers.filterIsInstance(CommandHandler::class.java)
            .groupBy({ it.commandScope }, { it.commandInfo })
    }

    suspend fun processUpdate(update: Update) {
        sentryClient.clearBreadcrumbs()

        sentryClient.setExtra("update", update.toString())

        if (update is UnknownUpdate) {
            log.error("Unknown update type: $update")

            sentryClient.captureEvent(SentryEvent().apply {
                message = Message().apply { message = "Unknown update type" }
                level = SentryLevel.ERROR
            })
            return
        }

        for (handler in handlers) {
            try {
                sentryClient.addBreadcrumb(handler::class.java.simpleName)
                val answered = handler.handleUpdate(update)
                if (answered) {
                    break
                }
            } catch (e: Exception) {
                log.error("Handler: ${handler::class.simpleName}, update: $update", e)

                exceptionsManager.process(e)

                sentryClient.captureException(e)
            }
        }
    }
}
