package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.query.CallbackQueryFilter
import com.github.djaler.evilbot.utils.decodeCallbackData
import com.github.djaler.evilbot.utils.isCallbackForHandler
import dev.inmo.tgbotapi.extensions.utils.asCallbackQueryUpdate
import dev.inmo.tgbotapi.extensions.utils.asMessageDataCallbackQuery
import dev.inmo.tgbotapi.types.UPDATE_CALLBACK_QUERY
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.update.abstracts.Update

abstract class CallbackQueryHandler(private val filter: CallbackQueryFilter? = null) : UpdateHandler {
    override val updateType get() = UPDATE_CALLBACK_QUERY

    override suspend fun handleUpdate(update: Update): Boolean {
        val callbackQuery = update.asCallbackQueryUpdate()?.data?.asMessageDataCallbackQuery() ?: return false

        if (filter?.filter(callbackQuery) == false) {
            return false
        }

        if (!isCallbackForHandler(callbackQuery.data, javaClass)) {
            return false
        }

        val data = decodeCallbackData(callbackQuery.data, javaClass)

        handleCallback(callbackQuery, data)

        return true
    }

    protected abstract suspend fun handleCallback(query: MessageDataCallbackQuery, data: String)
}
