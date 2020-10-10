package com.github.djaler.evilbot.handlers

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import dev.inmo.tgbotapi.types.chat.abstracts.extended.ExtendedPublicChat
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import dev.inmo.tgbotapi.types.message.abstracts.Message
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
