package com.github.djaler.evilbot.handlers.sed

import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.abstracts.MessageContent
import org.unix4j.Unix4j
import kotlin.reflect.KClass

interface SedTransformer<T : MessageContent> {
    val contentClass: KClass<T>

    suspend fun transformAndReply(content: T, args: String, replyTo: Message)

    fun applySed(text: String, args: String): String {
        return Unix4j.fromString(text).sed(args).toStringResult()
    }
}
