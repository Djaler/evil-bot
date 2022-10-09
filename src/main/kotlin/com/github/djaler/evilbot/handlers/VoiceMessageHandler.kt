package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.VoiceClient
import com.github.djaler.evilbot.config.vkcloud.VKCloudApiCondition
import com.github.djaler.evilbot.handlers.base.MessageHandler
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.withTypingAction
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asVoiceContent
import dev.inmo.tgbotapi.types.message.abstracts.Message
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component


@Component
@Conditional(VKCloudApiCondition::class)
class VoiceMessageHandler(
    private val requestExecutor: RequestsExecutor,
    private val voiceClient: VoiceClient
) : MessageHandler() {
    override suspend fun handleMessage(message: Message): Boolean {
        val voice = message.asContentMessage()?.content?.asVoiceContent() ?: return false

        val text = requestExecutor.withTypingAction(message.chat) {
            val file = requestExecutor.downloadFile(voice)
            voiceClient.getTextFromSpeech(file)
        }

        if (text.isNotEmpty()) {
            requestExecutor.reply(message, text = text)
            return true
        }
        return false
    }

}
