package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.PredictionService
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asTextContent
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import org.springframework.stereotype.Component

@Component
class ContinueHandler(
    private val requestsExecutor: RequestsExecutor,
    private val predictionService: PredictionService,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("continue"),
    commandDescription = "продолжить текст"
) {
    override suspend fun <M> handleCommand(
        message: M,
        args: String?
    ) where M : CommonMessage<TextContent>, M : FromUserMessage {
        val replyToText = message.replyTo?.asContentMessage()?.content?.asTextContent()?.text

        if ((args === null && replyToText === null)
            || (args !== null && replyToText !== null)
        ) {
            requestsExecutor.reply(message, "Либо пришли текст, либо ответь командой на другое сообщение")
            return
        }

        val text: String = (args ?: replyToText) as String

        val prediction = predictionService.getPrediction(text, leaveSource = false)

        requestsExecutor.reply(message, prediction)
    }
}