package com.github.djaler.evilbot.handlers

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.media.sendSticker
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class PayRespectHandler(
    private val requestsExecutor: RequestsExecutor,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("f"),
    commandDescription = "pay respect"
) {
    private val stickers by lazy {
        ClassPathResource("f_stickers.txt").inputStream.reader().readLines()
    }

    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val sticker = FileId(stickers.random())

        val replyTo = message.replyTo
        if (replyTo !== null) {
            requestsExecutor.sendSticker(replyTo.chat.id, sticker, replyToMessageId = replyTo.messageId)
        } else {
            requestsExecutor.sendSticker(message.chat.id, sticker)
        }
    }
}
