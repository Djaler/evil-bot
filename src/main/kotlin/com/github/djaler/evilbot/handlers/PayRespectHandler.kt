package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message

@Component
class PayRespectHandler(
    private val telegramClient: TelegramClient,
    botUsername: String
) : CommandHandler(botUsername, command = arrayOf("f")) {
    private val stickers by lazy {
        ClassPathResource("f_stickers.txt").inputStream.reader().readLines()
    }

    override fun handleCommand(message: Message, args: List<String>) {
        val sticker = stickers.random()
        if (message.isReply) {
            telegramClient.replyStickerTo(message.replyToMessage, sticker)
        } else {
            telegramClient.sendStickerTo(message.chatId, sticker)
        }
    }
}
