package com.github.djaler.evilbot.handlers.sed

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.VoiceContent
import org.springframework.stereotype.Component

@Component
class SedVoiceTransformer(
    private val requestsExecutor: RequestsExecutor
) : SedTransformer<VoiceContent> {
    override val contentClass = VoiceContent::class

    override suspend fun transformAndReply(content: VoiceContent, args: String, replyTo: Message) {
        content.text?.let {
            val result = applySed(it, args)
            requestsExecutor.reply(replyTo, content.media, result)
        }
    }
}
