package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.not
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User

@Component
class ReplyToPinHandler(
    private val telegramClient: TelegramClient,
    botInfo: User
) : CommandHandler(botInfo.userName, command = arrayOf("pinned"), filter = Filters.PrivateChat.not()) {
    override fun handleCommand(message: Message, args: List<String>) {
        val pinnedMessage: Message? = telegramClient.getChat(message.chatId).pinnedMessage

        if (pinnedMessage != null) {
            telegramClient.replyTextTo(pinnedMessage, "☝️️", disableNotification = true)
        } else {
            telegramClient.sendTextTo(message.chatId, "Закрепленное сообщение отсутствует")
        }
    }
}
