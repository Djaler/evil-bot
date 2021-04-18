package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.CatClient
import com.github.djaler.evilbot.utils.StorageFile
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.media.replyWithPhoto
import dev.inmo.tgbotapi.requests.abstracts.MultipartFile
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import org.springframework.stereotype.Component

@Component
class CatHandler(
    private val requestsExecutor: RequestsExecutor,
    private val catClient: CatClient,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("cat"),
    commandDescription = "сгенерировать котика"
) {
    override suspend fun <M> handleCommand(
        message: M,
        args: String?
    ) where M : CommonMessage<TextContent>, M : FromUserMessage {
        val catResponse = catClient.getCat()

        requestsExecutor.replyWithPhoto(
            message,
            MultipartFile(
                StorageFile(catResponse, "cat")
            )
        )
    }
}