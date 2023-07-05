package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.handlers.base.CommandHandler
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.TextMessage
import org.springframework.stereotype.Component

@Component
class ReplyToPinHandler(
    private val requestsExecutor: RequestsExecutor,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("pinned"),
    commandDescription = "указать на запиненное сообщение",
    commandScope = BotCommandScope.AllGroupChats,
) {
    override suspend fun handleCommand(
        message: TextMessage,
        args: String?
    ) {
        val chat = message.chat.asPublicChat() ?: return

        val extendedChat = requestsExecutor.getChat(chat)
        val pinnedMessage: Message? = extendedChat.pinnedMessage

        if (pinnedMessage != null) {
            requestsExecutor.reply(pinnedMessage, "☝️️", disableNotification = true)
        } else {
            requestsExecutor.sendMessage(chat.id, "Закрепленное сообщение отсутствует")
        }
    }
}
