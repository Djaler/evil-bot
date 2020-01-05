package com.github.djaler.evilbot.filters

import com.github.djaler.evilbot.components.TelegramClient
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.FromUserMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class ChatAdministratorFilter(
    private val telegramClient: TelegramClient
) : Filter {
    override suspend fun filter(message: Message): Boolean {
        if (message !is FromUserMessage) {
            return false
        }

        val chat = message.chat as? PublicChat ?: return false

        val chatAdministrators = telegramClient.getChatAdministrators(chat.id)

        return chatAdministrators.any { it.user.id == message.user.id }
    }
}
