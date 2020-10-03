package com.github.djaler.evilbot.filters.query

import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.members.getChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatMember.abstracts.AdministratorChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import org.springframework.stereotype.Component

@Component
class ChatAdministratorCallbackQueryFilter(
    private val requestsExecutor: RequestsExecutor
) : CallbackQueryFilter {
    override suspend fun filter(query: MessageCallbackQuery): Boolean {
        val chat = query.message.chat as? PublicChat ?: return false
        val memberInfo = requestsExecutor.getChatMember(chat.id, query.user.id)

        return memberInfo is AdministratorChatMember
    }
}
