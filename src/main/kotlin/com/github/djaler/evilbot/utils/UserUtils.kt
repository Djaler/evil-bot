package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.toChatId


val User.usernameOrName: String
    get() {
        username?.run { return usernameWithoutAt }

        return "$firstName $lastName".trim()
    }

val UserId.userId: Long
    get() = chatId

fun Long.toUserId(): UserId = toChatId()
