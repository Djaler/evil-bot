package com.github.djaler.evilbot.handlers.commands.sed.transformers

import com.github.djaler.evilbot.clients.SentryClient
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.message.abstracts.AccessibleMessage
import dev.inmo.tgbotapi.types.message.content.PollContent
import dev.inmo.tgbotapi.types.polls.InputPollOption
import dev.inmo.tgbotapi.types.polls.QuizPoll
import dev.inmo.tgbotapi.types.polls.RegularPoll
import dev.inmo.tgbotapi.types.polls.UnknownPollType
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component

@Component
class SedPollTransformer(
    private val requestsExecutor: RequestsExecutor,
    private val sentryClient: SentryClient,
) : SedTransformer<PollContent> {
    companion object {
        private val log = LogManager.getLogger()
    }

    override val contentClass = PollContent::class

    override suspend fun transformAndReply(content: PollContent, args: String, replyTo: AccessibleMessage) {
        val poll = content.poll
        val newQuestion = applySed(poll.question, args)
        val newOptions = poll.options.map {
            InputPollOption(applySed(it.text ?: "", args))
        }
        when (poll) {
            is RegularPoll -> {
                requestsExecutor.reply(
                    replyTo,
                    question = newQuestion,
                    options = newOptions,
                    isAnonymous = poll.isAnonymous,
                    allowsMultipleAnswers = poll.allowsMultipleAnswers
                )
            }
            is QuizPoll -> {
                requestsExecutor.reply(
                    replyTo,
                    quizPoll = poll,
                    question = newQuestion,
                    explanation = poll.explanation?.let { applySed(it, args) },
                    options = newOptions
                )
            }
            is UnknownPollType -> {
                log.error("Unknown poll type: $poll")

                sentryClient.setExtra("poll", poll.toString())
                sentryClient.captureEvent(SentryEvent().apply {
                    message = io.sentry.protocol.Message().apply { message = "Unknown poll type" }
                    level = SentryLevel.ERROR
                })
            }
        }
    }
}
