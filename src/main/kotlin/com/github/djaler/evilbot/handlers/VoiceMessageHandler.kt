package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.VoiceClient
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asVoiceContent
import dev.inmo.tgbotapi.types.message.abstracts.Message
import org.springframework.stereotype.Component


@Component
class VoiceMessageHandler(
    private val requestExecutor: RequestsExecutor,
    private val voiceClient: VoiceClient
) : MessageHandler() {
    override suspend fun handleMessage(message: Message): Boolean {
        val voice = message.asContentMessage()?.content?.asVoiceContent() ?: return false
        val file = requestExecutor.downloadFile(voice)
        val text = voiceClient.getTextFromSpeech(file)
        if (text.isNotEmpty()) {
            requestExecutor.reply(message, text = text)
            return true
        }
        return false
    }

}