package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.utils.decodeCallbackData
import com.github.djaler.evilbot.utils.isCallbackForHandler
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.update.CallbackQueryUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update

abstract class CallbackQueryHandler : UpdateHandler {
    override fun handleUpdate(update: Update): Boolean {
        if (update !is CallbackQueryUpdate) {
            return false
        }
        val callbackQuery = update.data as? MessageDataCallbackQuery ?: return false

        if (!isCallbackForHandler(callbackQuery.data, javaClass)) {
            return false
        }

        val data = decodeCallbackData(callbackQuery.data, javaClass)

        handleCallback(callbackQuery, data)

        return true
    }

    protected abstract fun handleCallback(query: MessageDataCallbackQuery, data: String)
}
