package com.github.djaler.evilbot.handlers

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicMessage
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.TextContent
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
    override suspend fun <M> handleCommand(
        message: M,
        args: String?
    ) where M : CommonMessage<TextContent>, M : FromUserMessage {
        val chat = message.asPublicMessage()?.chat ?: return

        val extendedChat = requestsExecutor.getChat(chat)
        val pinnedMessage: Message? = extendedChat.pinnedMessage

        if (pinnedMessage != null) {
            requestsExecutor.reply(pinnedMessage, "☝️️", disableNotification = true)
        } else {
            requestsExecutor.sendMessage(chat.id, "Закрепленное сообщение отсутствует")
        }
    }
}
