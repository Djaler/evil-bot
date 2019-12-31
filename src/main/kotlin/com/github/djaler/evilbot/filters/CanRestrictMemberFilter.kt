package com.github.djaler.evilbot.filters

import com.github.djaler.evilbot.components.TelegramClient
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatMember.abstracts.AdministratorChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class CanRestrictMemberFilter(
    private val telegramClient: TelegramClient,
    private val botInfo: User
) : Filter {
    override suspend fun filter(message: Message): Boolean {
        val botChatInfo = telegramClient.getChatMember(message.chat.id, botInfo.id)

        if (botChatInfo !is AdministratorChatMember) {
            return false
        }

        return botChatInfo.canRestrictMembers
    }
}
