package com.github.djaler.evilbot.filters.message

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.types.ChatMember.abstracts.AdministratorChatMember
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import dev.inmo.tgbotapi.types.message.abstracts.Message
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
