package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.Filter
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

abstract class MessageHandler(private val filter: Filter? = null) : UpdateHandler {
    override fun handleUpdate(update: Update): Boolean {
        if (!update.hasMessage()) {
            return false
        }

        if (filter?.filter(update.message) == false) {
            return false
        }

        return handleMessage(update.message)
    }

    abstract fun handleMessage(message: Message): Boolean
}
