package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.CatClient
import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.utils.asMultipartFile
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.replyWithPhoto
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
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
    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        val catResponse = catClient.getCat()

        requestsExecutor.replyWithPhoto(
            message,
            catResponse.asMultipartFile("cat")
        )
    }
}
