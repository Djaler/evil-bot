package com.github.djaler.evilbot.filters.message

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.types.ChatMember.abstracts.AdministratorChatMember
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class ChatAdministratorMessageFilter(
    private val requestsExecutor: RequestsExecutor
) : MessageFilter {
    override suspend fun filter(message: Message): Boolean {
        if (message !is FromUserMessage) {
            return false
        }

        val chat = message.chat as? PublicChat ?: return false
        val memberInfo = requestsExecutor.getChatMember(chat.id, message.user.id)

        return memberInfo is AdministratorChatMember
    }
}
