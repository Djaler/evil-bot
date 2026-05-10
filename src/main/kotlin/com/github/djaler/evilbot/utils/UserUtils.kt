package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.toChatId


val User.usernameOrName: String
    get() {
        username?.run { return withoutAt }

        return "$firstName $lastName".trim()
    }

val UserId.userId: Long
    get() = chatId.long

fun Long.toUserId(): UserId = toChatId()
