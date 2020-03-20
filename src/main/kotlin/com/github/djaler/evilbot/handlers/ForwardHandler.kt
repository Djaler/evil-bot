package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.utils.usernameOrName
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import org.springframework.stereotype.Component

@Component
class ForwardHandler(
    private val telegramClient: TelegramClient,
    botInfo: ExtendedBot
) : CommandHandler(botInfo, command = arrayOf("me")) {
    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        if (args === null) {
            telegramClient.replyTextTo(message, "И что я должен отправить, по твоему?")
            return
        }

        telegramClient.sendTextTo(message.chat.id, message.user.usernameOrName + " " + args)
        telegramClient.deleteMessage(message)
    }
}
