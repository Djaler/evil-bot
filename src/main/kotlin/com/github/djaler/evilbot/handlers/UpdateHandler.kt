package com.github.djaler.evilbot.handlers

import org.telegram.telegrambots.meta.api.objects.Update


interface UpdateHandler {
    fun handleUpdate(update: Update): Boolean

    val order get() = 1
}
