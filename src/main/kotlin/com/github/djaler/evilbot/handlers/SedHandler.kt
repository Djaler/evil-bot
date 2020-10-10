package com.github.djaler.evilbot.handlers

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.media.*
import dev.inmo.tgbotapi.extensions.api.send.polls.sendQuizPoll
import dev.inmo.tgbotapi.extensions.api.send.polls.sendRegularPoll
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.files.AnimationFile
import dev.inmo.tgbotapi.types.files.AudioFile
import dev.inmo.tgbotapi.types.files.VideoFile
import dev.inmo.tgbotapi.types.files.VoiceFile
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.PollContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.media.*
import dev.inmo.tgbotapi.types.polls.*
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
                is AnimationContent -> {
                    content.caption?.let {
                        handleAnimation(content.media, it, args, replyTo)
                    }
                }
                is VideoContent -> {
                    content.caption?.let {
                        handleVideo(content.media, it, args, replyTo)
                    }
                }
                is PollContent -> {
                    handlePoll(content.poll, args, replyTo)
                }
                is VoiceContent -> {
                    content.caption?.let {
                        handleVoice(content.media, it, args, replyTo)
                    }
                }
                is AudioContent -> {
                    content.caption?.let {
                        handleAudio(content.media, it, args, replyTo)
                    }
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

    private suspend fun handleAnimation(
        animationFile: AnimationFile,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.sendAnimation(replyTo.chat.id, animationFile, result, replyToMessageId = replyTo.messageId)
    }

    private suspend fun handleVideo(
        videoFile: VideoFile,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.sendVideo(replyTo.chat.id, videoFile, result, replyToMessageId = replyTo.messageId)
    }

    private suspend fun handleAudio(
        audioFile: AudioFile,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.sendAudio(replyTo.chat.id, audioFile, text = result, replyToMessageId = replyTo.messageId)
    }

    private suspend fun handleVoice(
        voiceFile: VoiceFile,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.sendVoice(replyTo.chat.id, voiceFile.fileId, text = result, replyToMessageId = replyTo.messageId)
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
