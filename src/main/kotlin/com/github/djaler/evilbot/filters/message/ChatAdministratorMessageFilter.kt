package com.github.djaler.evilbot.filters.message

import com.github.djaler.evilbot.components.TelegramClient
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatMember.abstracts.AdministratorChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.FromUserMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class ChatAdministratorMessageFilter(
    private val telegramClient: TelegramClient
) : MessageFilter {
    override suspend fun filter(message: Message): Boolean {
        if (message !is FromUserMessage) {
            return false
        }

        val chat = message.chat as? PublicChat ?: return false
        val memberInfo = telegramClient.getChatMember(chat.id, message.user.id)

        return memberInfo is AdministratorChatMember
    }
}
