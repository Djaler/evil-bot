package com.github.djaler.evilbot.utils

import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.UserId


val User.usernameOrName: String
    get() {
        username?.run { return username.trimStart('@') }

        return "$firstName $lastName".trim()
    }

val UserId.userId: Int
    get() = chatId.toInt()
