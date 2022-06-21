package com.github.djaler.evilbot.handlers.sed

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.VideoContent
import org.springframework.stereotype.Component

@Component
class SedVideoTransformer(
    private val requestsExecutor: RequestsExecutor
) : SedTransformer<VideoContent> {
    override val contentClass = VideoContent::class

    override suspend fun transformAndReply(content: VideoContent, args: String, replyTo: Message) {
        content.text?.let {
            val result = applySed(it, args)
            requestsExecutor.reply(replyTo, content.media, result)
        }
    }
}
