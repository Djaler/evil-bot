package com.github.djaler.evilbot.handlers.sed

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.AnimationContent
import org.springframework.stereotype.Component

@Component
class SedAnimationTransformer(
    private val requestsExecutor: RequestsExecutor
) : SedTransformer<AnimationContent> {
    override val contentClass = AnimationContent::class

    override suspend fun transformAndReply(content: AnimationContent, args: String, replyTo: Message) {
        content.text?.let {
            val result = applySed(it, args)
            requestsExecutor.reply(replyTo, content.media, result)
        }
    }
}
