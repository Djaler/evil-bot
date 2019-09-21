package com.github.djaler.evilbot.filters

import com.github.djaler.evilbot.components.TelegramClient
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User

@Component
class CanRestrictMemberFilter(
    private val telegramClient: TelegramClient,
    private val botInfo: User
) : Filter {
    override fun filter(message: Message): Boolean {
        val botChatInfo = telegramClient.getChatMember(message.chatId, botInfo.id)

        return botChatInfo.canRestrictUsers ?: false
    }
}
