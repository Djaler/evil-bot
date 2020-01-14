package com.github.djaler.evilbot.utils

import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.UserId
import com.github.insanusmokrassar.TelegramBotAPI.types.toChatId


val User.usernameOrName: String
    get() {
        username?.run { return username.trimStart('@') }

        return "$firstName $lastName".trim()
    }

val UserId.userId: Int
    get() = chatId.toInt()

fun Int.toUserId(): UserId = toChatId()
