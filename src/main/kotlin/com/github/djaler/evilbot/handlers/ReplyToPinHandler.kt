package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.extended.ExtendedPublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class ReplyToPinHandler(
    private val telegramClient: TelegramClient,
    botInfo: User
) : CommandHandler(botInfo, command = arrayOf("pinned")) {
    override fun handleCommand(message: CommonMessageImpl<*>, args: List<String>) {
        val chat = message.chat as? PublicChat ?: return

        val extendedChat = telegramClient.getChat(chat.id) as ExtendedPublicChat
        val pinnedMessage: Message? = extendedChat.pinnedMessage

        if (pinnedMessage != null) {
            telegramClient.replyTextTo(pinnedMessage, "☝️️", disableNotification = true)
        } else {
            telegramClient.sendTextTo(chat.id, "Закрепленное сообщение отсутствует")
        }
    }
}
