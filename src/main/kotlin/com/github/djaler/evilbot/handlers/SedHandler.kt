package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import org.springframework.stereotype.Component
import org.unix4j.Unix4j

@Component
class SedHandler(
    private val telegramClient: TelegramClient,
    botInfo: User
) : CommandHandler(botInfo, command = arrayOf("sed")) {
    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        if (args === null) {
            return
        }

        val replyTo = message.replyTo as? ContentMessage<*> ?: return
        val content = replyTo.content as? TextContent ?: return

        val result = try {
            Unix4j.fromString(content.text).sed(args).toStringResult()
        } catch (e: IllegalArgumentException) {
            e.localizedMessage
        }

        telegramClient.replyTextTo(replyTo, result)
    }
}
