package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.utils.usernameOrName
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User

@Component
class ForwardHandler(
    private val telegramClient: TelegramClient,
    botInfo: User
) : CommandHandler(botInfo.userName, command = arrayOf("me")) {
    override fun handleCommand(message: Message, args: List<String>) {
        telegramClient.sendTextTo(message.chatId, message.from.usernameOrName + " " + args.joinToString(" "))
        telegramClient.deleteMessage(message)
    }
}
