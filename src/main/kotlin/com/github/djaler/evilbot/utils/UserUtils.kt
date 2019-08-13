package com.github.djaler.evilbot.utils

import org.telegram.telegrambots.meta.api.objects.User

val User.usernameOrName: String
    get() {
        if (userName != null) {
            return userName
        }
        if (lastName != null) {
            return "$firstName $lastName"
        }

        return firstName
    }
