package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.asFromUserMessage
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
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
    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        val user = message.asFromUserMessage()?.user ?: return

        if (args === null) {
            requestsExecutor.reply(message, "И что я должен отправить, по твоему?")
            return
        }

        requestsExecutor.sendMessage(message.chat.id, user.usernameOrName + " " + args)
        requestsExecutor.deleteMessage(message)
    }
}
