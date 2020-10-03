package com.github.djaler.evilbot.filters.message

import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.members.getChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatMember.abstracts.AdministratorChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class CanRestrictMemberMessageFilter(
    private val requestsExecutor: RequestsExecutor,
    private val botInfo: ExtendedBot
) : MessageFilter {
    override suspend fun filter(message: Message): Boolean {
        val chat = message.chat as? PublicChat ?: return false

        val botChatInfo = requestsExecutor.getChatMember(chat.id, botInfo.id)

        if (botChatInfo !is AdministratorChatMember) {
            return false
        }

        return botChatInfo.canRestrictMembers
    }
}
