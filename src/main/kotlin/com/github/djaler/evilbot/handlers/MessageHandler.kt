package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.message.MessageFilter
import dev.inmo.tgbotapi.types.UPDATE_MESSAGE
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.update.MessageUpdate
import dev.inmo.tgbotapi.types.update.abstracts.Update

abstract class MessageHandler(private val filter: MessageFilter? = null) : UpdateHandler {
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

abstract class CommonMessageHandler(private val filter: MessageFilter? = null) : MessageHandler(filter) {
    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is CommonMessage<*>) {
            return false
        }

        return handleMessage(message)
    }

    protected abstract suspend fun handleMessage(message: CommonMessage<*>): Boolean
}
