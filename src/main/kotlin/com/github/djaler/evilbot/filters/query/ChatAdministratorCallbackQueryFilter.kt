package com.github.djaler.evilbot.filters.query

import com.github.djaler.evilbot.components.TelegramClient
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatMember.abstracts.AdministratorChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import org.springframework.stereotype.Component

@Component
class ChatAdministratorCallbackQueryFilter(
    private val telegramClient: TelegramClient
) : CallbackQueryFilter {
    override suspend fun filter(query: MessageCallbackQuery): Boolean {
        val chat = query.message.chat as? PublicChat ?: return false
        val memberInfo = telegramClient.getChatMember(chat.id, query.user.id)

        return memberInfo is AdministratorChatMember
    }
}
