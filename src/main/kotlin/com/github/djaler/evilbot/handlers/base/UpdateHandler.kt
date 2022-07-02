package com.github.djaler.evilbot.handlers.base

import dev.inmo.tgbotapi.types.update.abstracts.Update


interface UpdateHandler {
    suspend fun handleUpdate(update: Update): Boolean

    val updateType: String

    val order get() = 1
}
