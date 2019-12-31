package com.github.djaler.evilbot.utils

import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.UserId


val User.usernameOrName: String
    get() {
        username?.run { return username.trimStart('@') }

        lastName?.run { return "$firstName $lastName" }

        return firstName
    }

val UserId.userId: Int
    get() = chatId.toInt()
