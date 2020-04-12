package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.Filter
import com.github.insanusmokrassar.TelegramBotAPI.types.UPDATE_MESSAGE
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update

abstract class MessageHandler(private val filter: Filter? = null) : UpdateHandler {
    override val updateType get() = UPDATE_MESSAGE

    override suspend fun handleUpdate(update: Update): Boolean {
        if (update !is MessageUpdate) {
            return false
        }

        if (filter?.filter(update.data) == false) {
            return false
        }

        return handleMessage(update.data)
    }

    protected abstract suspend fun handleMessage(message: Message): Boolean
}

abstract class CommonMessageHandler(private val filter: Filter? = null) : MessageHandler(filter) {
    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is CommonMessageImpl<*>) {
            return false
        }

        return handleMessage(message)
    }

    protected abstract suspend fun handleMessage(message: CommonMessageImpl<*>): Boolean
}
