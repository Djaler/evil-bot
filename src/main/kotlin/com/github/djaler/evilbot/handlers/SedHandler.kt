package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.sed.SedTransformerFactory
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import org.springframework.stereotype.Component

@Component
class SedHandler(
    private val requestsExecutor: RequestsExecutor,
    private val sedTransformerFactory: SedTransformerFactory,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("sed"),
    commandDescription = "преобразовать строку с помощью sed"
) {
    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        if (args === null) {
            requestsExecutor.reply(message, "Ну а где выражение для sed?")
            return
        }

        val replyTo = message.replyTo?.asContentMessage() ?: return

        val content = replyTo.content
        try {
            sedTransformerFactory.getForContent(content)
                ?.transformAndReply(content, args, replyTo)
        } catch (e: IllegalArgumentException) {
            requestsExecutor.reply(message, e.localizedMessage)
        }
    }
}
