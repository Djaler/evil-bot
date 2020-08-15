package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.PollContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.polls.*
import io.sentry.SentryClient
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import org.unix4j.Unix4j

@Component
class SedHandler(
    private val telegramClient: TelegramClient,
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
            telegramClient.replyTextTo(message, "Ну а где выражение для sed?")
            return
        }

        val replyTo = message.replyTo as? ContentMessage<*> ?: return

        val content = replyTo.content
        try {
            when (content) {
                is TextContent -> {
                    handleText(content.text, args, replyTo)
                }
                is PollContent -> {
                    handlePoll(content.poll, args, replyTo)
                }
            }
        } catch (e: IllegalArgumentException) {
            telegramClient.replyTextTo(message, e.localizedMessage)
        }
    }

    private suspend fun handleText(
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        telegramClient.replyTextTo(replyTo, result)
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
                telegramClient.replyPollTo(replyTo, newPoll)
            }
            is QuizPoll -> {
                val newPoll = poll.copy(
                    question = newQuestion,
                    options = newOptions,
                    explanation = poll.explanation?.let { applySed(it, args) }
                )
                telegramClient.replyPollTo(replyTo, newPoll)
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
