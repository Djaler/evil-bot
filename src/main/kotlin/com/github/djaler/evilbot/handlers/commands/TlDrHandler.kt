package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.config.yandex.YandexApiCondition
import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.service.YandexGptService
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.withTypingAction
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asURLTextSource
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.TextMessage
import org.apache.logging.log4j.LogManager
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(YandexApiCondition::class)
class TlDrHandler(
    private val requestsExecutor: RequestsExecutor,
    private val yandexGptService: YandexGptService,
    private val sentryClient: SentryClient,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("tldr"),
    commandDescription = "пересказать содержимое по ссылке"
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    override suspend fun handleCommand(
        message: TextMessage,
        args: String?
    ) {
        val messageToReply: Message
        val link: String?

        val replyTo = message.replyTo
        val replyMessageLink = replyTo?.asContentMessage()?.let { extractLink(it) }
        if (replyMessageLink !== null) {
            messageToReply = replyTo
            link = replyMessageLink
        } else {
            messageToReply = message
            link = extractLink(message)
        }

        if (link === null) {
            requestsExecutor.reply(messageToReply, "Либо пришли ссылку, либо ответь командой на сообщение со ссылкой")
            return
        }

        requestsExecutor.withTypingAction(message.chat) {
            try {
                val thesis = yandexGptService.generateLinkThesis(link)
                if (thesis != null) {
                    requestsExecutor.reply(messageToReply, thesis)
                } else {
                    log.warn("Empty thesis generation result")
                }
            } catch (e: Exception) {
                log.error("Exception in thesis generation", e)
                sentryClient.captureException(e)
                requestsExecutor.reply(messageToReply, "Не получилось, попробуй ещё")
            }
        }
    }

    private fun extractLink(message: ContentMessage<*>): String? {
        return when (val content = message.content) {
            is TextContent -> content.textSources.firstNotNullOfOrNull { it.asURLTextSource()?.source }
            else -> null
        }
    }
}
