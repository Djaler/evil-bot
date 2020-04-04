package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.FileId
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class PayRespectHandler(
    private val telegramClient: TelegramClient,
    botInfo: ExtendedBot
) : CommandHandler(botInfo, command = arrayOf("f")) {
    private val stickers by lazy {
        ClassPathResource("f_stickers.txt").inputStream.reader().readLines()
    }

    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val sticker = FileId(stickers.random())

        val replyTo = message.replyTo
        if (replyTo !== null) {
            telegramClient.replyStickerTo(replyTo, sticker)
        } else {
            telegramClient.sendStickerTo(message.chat.id, sticker)
        }
    }
}
