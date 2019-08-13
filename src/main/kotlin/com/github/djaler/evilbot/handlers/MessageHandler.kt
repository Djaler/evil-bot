package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.Filter
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

abstract class MessageHandler(private val filter: Filter? = null) : UpdateHandler {
    override fun checkUpdate(update: Update): Boolean {
        if (!update.hasMessage()) {
            return false
        }

        return filter?.filter(update.message) ?: true
    }

    override fun handleUpdate(update: Update): Boolean {
        return handleMessage(update.message)
    }

    abstract fun handleMessage(message: Message): Boolean
}
