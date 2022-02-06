package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.SentryClient
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.*
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.files.AnimationFile
import dev.inmo.tgbotapi.types.files.AudioFile
import dev.inmo.tgbotapi.types.files.VideoFile
import dev.inmo.tgbotapi.types.files.VoiceFile
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.PollContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.media.*
import dev.inmo.tgbotapi.types.polls.*
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
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

    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        if (args === null) {
            requestsExecutor.reply(message, "Ну а где выражение для sed?")
            return
        }

        val replyTo = message.replyTo?.asContentMessage() ?: return

        val content = replyTo.content
        try {
            when (content) {
                is TextContent -> {
                    handleText(content.text, args, replyTo)
                }
                is PhotoContent -> {
                    content.text?.let {
                        handlePhoto(content.media.fileId, it, args, replyTo)
                    }
                }
                is AnimationContent -> {
                    content.text?.let {
                        handleAnimation(content.media, it, args, replyTo)
                    }
                }
                is VideoContent -> {
                    content.text?.let {
                        handleVideo(content.media, it, args, replyTo)
                    }
                }
                is PollContent -> {
                    handlePoll(content.poll, args, replyTo)
                }
                is VoiceContent -> {
                    content.text?.let {
                        handleVoice(content.media, it, args, replyTo)
                    }
                }
                is AudioContent -> {
                    content.text?.let {
                        handleAudio(content.media, it, args, replyTo)
                    }
                }
            }
        } catch (e: IllegalArgumentException) {
            requestsExecutor.reply(message, e.localizedMessage)
        }
    }

    private suspend fun handlePhoto(
        fileId: FileId,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.replyWithPhoto(replyTo, fileId, result)
    }

    private suspend fun handleAnimation(
        animationFile: AnimationFile,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.replyWithAnimation(replyTo, animationFile.fileId, text = result)
    }

    private suspend fun handleVideo(
        videoFile: VideoFile,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.replyWithVideo(replyTo, videoFile.fileId, text = result)
    }

    private suspend fun handleAudio(
        audioFile: AudioFile,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.replyWithAudio(replyTo, audioFile.fileId, text = result)
    }

    private suspend fun handleVoice(
        voiceFile: VoiceFile,
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.replyWithVoice(replyTo, voiceFile.fileId, text = result)
    }

    private suspend fun handleText(
        text: String,
        args: String,
        replyTo: ContentMessage<*>
    ) {
        val result = applySed(text, args)
        requestsExecutor.reply(replyTo, result)
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
                    message = Message().apply { message = "Unknown poll type" }
                    level = SentryLevel.ERROR
                })
            }
        }
    }

    private fun applySed(text: String, args: String): String {
        return Unix4j.fromString(text).sed(args).toStringResult()
    }
}
