package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.service.PredictionService
import dev.inmo.tgbotapi.CommonAbstracts.Texted
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.withTypingAction
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.textLength
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component

@Component
class ContinueHandler(
    private val requestsExecutor: RequestsExecutor,
    private val predictionService: PredictionService,
    private val sentryClient: SentryClient,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("continue"),
    commandDescription = "продолжить текст"
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    override suspend fun <M> handleCommand(
        message: M,
        args: String?
    ) where M : CommonMessage<TextContent>, M : FromUserMessage {
        var messageToReply: Message = message
        var sourceText = args

        val replyTo = message.replyTo
        val replyMessageText = replyTo?.asContentMessage()?.let { extractMessageText(it) }
        if (replyMessageText !== null) {
            messageToReply = replyTo
            sourceText = replyMessageText
        }

        if (sourceText === null) {
            requestsExecutor.reply(messageToReply, "Либо пришли текст, либо ответь командой на текстовое сообщение")
            return
        }

        requestsExecutor.withTypingAction(message.chat) {
            try {
                val prediction = predictionService.getPrediction(sourceText, leaveSource = false)

                prediction.chunked(textLength.last).forEach { requestsExecutor.reply(messageToReply, it) }
            } catch (e: Exception) {
                log.error("Exception in prediction generation", e)
                sentryClient.captureException(e)
                requestsExecutor.reply(messageToReply, "Не получилось, попробуй ещё")
            }
        }
    }

    private fun extractMessageText(message: ContentMessage<*>): String? {
        return when (val content = message.content) {
            is Texted -> content.text
            else -> null
        }
    }
}
