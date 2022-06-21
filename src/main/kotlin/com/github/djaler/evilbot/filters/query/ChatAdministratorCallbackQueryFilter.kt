package com.github.djaler.evilbot.filters.query

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.types.chat.PublicChat
import dev.inmo.tgbotapi.types.chat.member.AdministratorChatMember
import dev.inmo.tgbotapi.types.queries.callback.MessageCallbackQuery
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
