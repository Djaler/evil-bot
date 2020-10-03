package com.github.djaler.evilbot.handlers

import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.get.getChat
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.extended.ExtendedPublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class ReplyToPinHandler(
    private val requestsExecutor: RequestsExecutor,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("pinned"),
    commandDescription = "указать на запиненное сообщение"
) {
    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val chat = message.chat as? PublicChat ?: return

        val extendedChat = requestsExecutor.getChat(chat.id) as ExtendedPublicChat
        val pinnedMessage: Message? = extendedChat.pinnedMessage

        if (pinnedMessage != null) {
            requestsExecutor.sendMessage(chatId = chat.id, replyToMessageId = pinnedMessage.messageId, text = "☝️️", disableNotification = true)
        } else {
            requestsExecutor.sendMessage(chat.id, "Закрепленное сообщение отсутствует")
        }
    }
}
