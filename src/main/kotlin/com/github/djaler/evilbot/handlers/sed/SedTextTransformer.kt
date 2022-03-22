package com.github.djaler.evilbot.handlers.sed

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.TextContent
import org.springframework.stereotype.Component

@Component
class SedTextTransformer(
    private val requestsExecutor: RequestsExecutor
) : SedTransformer<TextContent> {
    override val contentClass = TextContent::class

    override suspend fun transformAndReply(content: TextContent, args: String, replyTo: Message) {
        val result = applySed(content.text, args)
        requestsExecutor.reply(replyTo, result)
    }
}
