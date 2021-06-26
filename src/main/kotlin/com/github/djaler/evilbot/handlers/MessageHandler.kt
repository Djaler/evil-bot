package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.message.MessageFilter
import dev.inmo.tgbotapi.extensions.utils.asCommonMessage
import dev.inmo.tgbotapi.extensions.utils.asMessageUpdate
import dev.inmo.tgbotapi.types.UPDATE_MESSAGE
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.update.abstracts.Update

abstract class MessageHandler(private val filter: MessageFilter? = null) : UpdateHandler {
    override val updateType get() = UPDATE_MESSAGE

    override suspend fun handleUpdate(update: Update): Boolean {
        val message = update.asMessageUpdate()?.data ?: return false

        if (filter?.filter(message) == false) {
            return false
        }

        return handleMessage(message)
    }

    protected abstract suspend fun handleMessage(message: Message): Boolean
}

abstract class CommonMessageHandler(filter: MessageFilter? = null) : MessageHandler(filter) {
    override suspend fun handleMessage(message: Message): Boolean {
        val commonMessage = message.asCommonMessage() ?: return false

        return handleMessage(commonMessage)
    }

    protected abstract suspend fun handleMessage(message: CommonMessage<*>): Boolean
}
