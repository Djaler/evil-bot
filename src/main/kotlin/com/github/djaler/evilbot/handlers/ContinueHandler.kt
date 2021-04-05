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
import dev.inmo.tgbotapi.types.textLength
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

        val sourceText = args ?: replyToText

        if (sourceText === null || (args !== null && replyToText !== null)) {
            requestsExecutor.reply(message, "Либо пришли текст, либо ответь командой на текстовое сообщение")
            return
        }

        try {
            val prediction = predictionService.getPrediction(sourceText, leaveSource = false)

            prediction.chunked(textLength.last).forEach { requestsExecutor.reply(message, it) }
        } catch (e: Exception) {
            requestsExecutor.reply(message, "Не получилось, попробуй ещё")
        }
    }
}