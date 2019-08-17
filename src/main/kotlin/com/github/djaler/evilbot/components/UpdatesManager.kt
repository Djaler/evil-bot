package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.handlers.UpdateHandler
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class UpdatesManager(handlers: List<UpdateHandler>) {
    companion object {
        private val log = LogManager.getLogger()
    }

    private val handlers = handlers.sortedBy { it.order }

    fun processUpdate(update: Update) {
        for (handler in handlers) {
            try {
                if (handler.checkUpdate(update)) {
                    val answered = handler.handleUpdate(update)
                    if (answered) {
                        break
                    }

                }
            } catch (e: Exception) {
                log.error("Handler: ${handler::class.simpleName}, update: $update", e)
            }
        }
    }
}
