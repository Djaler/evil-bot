package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.handlers.base.CommandHandler
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.media.sendSticker
import dev.inmo.tgbotapi.extensions.api.send.replyWithSticker
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.content.TextMessage
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

    override suspend fun handleCommand(
        message: TextMessage,
        args: String?
    ) {
        val sticker = FileId(stickers.random())

        val replyTo = message.replyTo
        if (replyTo !== null) {
            requestsExecutor.replyWithSticker(replyTo, sticker)
        } else {
            requestsExecutor.sendSticker(message.chat.id, sticker)
        }
    }
}
