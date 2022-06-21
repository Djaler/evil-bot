package com.github.djaler.evilbot.handlers.sed

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import org.springframework.stereotype.Component

@Component
class SedPhotoTransformer(
    private val requestsExecutor: RequestsExecutor
) : SedTransformer<PhotoContent> {
    override val contentClass = PhotoContent::class

    override suspend fun transformAndReply(content: PhotoContent, args: String, replyTo: Message) {
        content.text?.let {
            val result = applySed(it, args)
            requestsExecutor.reply(replyTo, content.media, result)
        }
    }
}
