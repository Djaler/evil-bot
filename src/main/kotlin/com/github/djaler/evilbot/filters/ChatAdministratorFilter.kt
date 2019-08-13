package com.github.djaler.evilbot.filters

import com.github.djaler.evilbot.components.TelegramClient
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message

@Component
class ChatAdministratorFilter(
    private val telegramClient: TelegramClient
) : Filter {
    override fun filter(message: Message): Boolean {
        val chatAdministrators = telegramClient.getChatAdministrators(message.chatId)

        return chatAdministrators.any { it.user.id == message.from.id }
    }
}
