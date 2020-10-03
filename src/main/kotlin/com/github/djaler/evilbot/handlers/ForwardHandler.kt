package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.utils.usernameOrName
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.deleteMessage
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import org.springframework.stereotype.Component

@Component
class ForwardHandler(
    private val requestsExecutor: RequestsExecutor,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("me"),
    commandDescription = "отправить сообщение от имени бота"
) {
    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        if (args === null) {
            requestsExecutor.sendMessage(chatId = message.chat.id, text = "И что я должен отправить, по твоему?", replyToMessageId = message.messageId)
            return
        }

        requestsExecutor.sendMessage(message.chat.id, message.user.usernameOrName + " " + args)
        requestsExecutor.deleteMessage(message)
    }
}
