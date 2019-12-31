package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.Filter
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update

abstract class MessageHandler(private val filter: Filter? = null) : UpdateHandler {
    override suspend fun handleUpdate(update: Update): Boolean {
        if (update !is MessageUpdate) {
            return false
        }

        if (filter?.filter(update.data) == false) {
            return false
        }

        return handleMessage(update.data)
    }

    abstract suspend fun handleMessage(message: Message): Boolean
}
