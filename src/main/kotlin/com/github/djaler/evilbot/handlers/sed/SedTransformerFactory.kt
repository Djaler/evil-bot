package com.github.djaler.evilbot.handlers.sed

import dev.inmo.tgbotapi.types.message.content.abstracts.MessageContent
import org.springframework.stereotype.Component

@Component
class SedTransformerFactory(
    private val transformers: List<SedTransformer<*>>
) {
    final fun <T : MessageContent> getForContent(content: T): SedTransformer<T>? {
        return transformers
            .firstOrNull { it.contentClass.isInstance(content) }
            ?.let { it as SedTransformer<T> }
    }
}
