package com.github.djaler.evilbot.handlers

import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update


interface UpdateHandler {
    suspend fun handleUpdate(update: Update): Boolean

    val order get() = 1
}
