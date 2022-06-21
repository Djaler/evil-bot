package com.github.djaler.evilbot.handlers.sed

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.AudioContent
import org.springframework.stereotype.Component

@Component
class SedAudioTransformer(
    private val requestsExecutor: RequestsExecutor
) : SedTransformer<AudioContent> {
    override val contentClass = AudioContent::class

    override suspend fun transformAndReply(content: AudioContent, args: String, replyTo: Message) {
        content.text?.let {
            val result = applySed(it, args)
            requestsExecutor.reply(replyTo, content.media, result)
        }
    }
}
