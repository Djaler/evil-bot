package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.types.User
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.toChatId


val User.usernameOrName: String
    get() {
        username?.run { return username.trimStart('@') }

        return "$firstName $lastName".trim()
    }

val UserId.userId: Int
    get() = chatId.toInt()

fun Int.toUserId(): UserId = toChatId()
