package com.github.djaler.evilbot.handlers

import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendPhoto
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.polls.sendQuizPoll
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.polls.sendRegularPoll
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.FileId
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.PollContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.media.PhotoContent
import com.github.insanusmokrassar.TelegramBotAPI.types.polls.*
import io.sentry.SentryClient
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import org.unix4j.Unix4j

@Component
class SedHandler(
    private val requestsExecutor: RequestsExecutor,
    private val sentryClient: SentryClient,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("sed"),
    commandDescription = "преобразовать строку с помощью sed"
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        if (args === null) {
            requestsExecutor.sendMessage(message.chat.id, "Ну а где выражение для sed?", replyToMessageId = message.messageId)
            return
        }

        val replyTo = message.replyTo as? ContentMessage<*> ?: return

        val content = replyTo.content
        try {
            when (content) {
                is TextContent -> {
                    handleText(content.text, args, replyTo)
                }
                is PhotoContent -> {
                    content.caption?.let {
                        handlePhoto(content.media.fileId, it, args, replyTo)
                    }
                }
                is PollContent -> {
                    handlePoll(content.poll, args, replyTo)
                }
            }
        } catch (e: IllegalArgumentException) {
            requestsExecutor.sendMessage(message.chat.id, e.localizedMessage, replyToMessageId = message.messageId)
        }
    }

    private suspend fun handlePhoto(
        fileId: FileId,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.sendPhoto(replyTo.chat.id, fileId, result, replyToMessageId = replyTo.messageId)
    }

    private suspend fun handleText(
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.sendMessage(replyTo.chat.id, result, replyToMessageId = replyTo.messageId)
    }

    private suspend fun handlePoll(
        poll: Poll,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val newQuestion = applySed(poll.question, args)
        val newOptions = poll.options.map {
            SimplePollOption(
                text = applySed(it.text, args),
                votes = 0 // not really used
            )
        }
        when (poll) {
            is RegularPoll -> {
                val newPoll = poll.copy(
                    question = newQuestion,
                    options = newOptions
                )
                requestsExecutor.sendRegularPoll(replyTo.chat, poll = newPoll, replyToMessageId = replyTo.messageId)
            }
            is QuizPoll -> {
                val newPoll = poll.copy(
                    question = newQuestion,
                    options = newOptions,
                    explanation = poll.explanation?.let { applySed(it, args) }
                )
                requestsExecutor.sendQuizPoll(replyTo.chat, quizPoll = newPoll)
            }
            is UnknownPollType -> {
                log.error("Unknown poll type: $poll")

                sentryClient.context.addExtra("poll", poll)
                sentryClient.sendEvent(
                    EventBuilder()
                        .withMessage("Unknown poll type")
                        .withLevel(Event.Level.ERROR)
                        .build()
                )
            }
        }
    }

    private fun applySed(text: String, args: String): String {
        return Unix4j.fromString(text).sed(args).toStringResult()
    }
}
