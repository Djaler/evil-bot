package com.github.djaler.evilbot.handlers.sed

import com.github.djaler.evilbot.clients.SentryClient
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.PollContent
import dev.inmo.tgbotapi.types.polls.QuizPoll
import dev.inmo.tgbotapi.types.polls.RegularPoll
import dev.inmo.tgbotapi.types.polls.SimplePollOption
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

    override suspend fun transformAndReply(content: PollContent, args: String, replyTo: Message) {
        val poll = content.poll
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
                requestsExecutor.reply(replyTo, newPoll)
            }
            is QuizPoll -> {
                val newPoll = poll.copy(
                    question = newQuestion,
                    options = newOptions,
                    text = poll.text?.let { applySed(it, args) }
                )
                requestsExecutor.reply(replyTo, quizPoll = newPoll)
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
