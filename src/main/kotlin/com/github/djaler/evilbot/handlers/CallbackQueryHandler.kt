package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.utils.decodeCallbackData
import com.github.djaler.evilbot.utils.isCallbackForHandler
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update

abstract class CallbackQueryHandler : UpdateHandler {
    override fun handleUpdate(update: Update): Boolean {
        if (!update.hasCallbackQuery()) {
            return false
        }

        if (!isCallbackForHandler(update.callbackQuery.data, javaClass)) {
            return false
        }

        val data = decodeCallbackData(update.callbackQuery.data, javaClass)

        handleCallback(update.callbackQuery, data)

        return true
    }

    protected abstract fun handleCallback(query: CallbackQuery, data: String)
}
