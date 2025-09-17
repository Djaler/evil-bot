package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.base.CommonMessageHandler
import com.github.djaler.evilbot.config.VideoDownloadProperties
import com.github.djaler.evilbot.service.YtDlpService
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.media.sendVideo
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.withUploadVideoAction
import dev.inmo.tgbotapi.extensions.utils.asTextContent
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import org.springframework.stereotype.Component
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.utils.PreviewFeature

@Component
class VideoLinkDownloadHandler(
    private val requestsExecutor: RequestsExecutor,
    private val ytDlpService: YtDlpService,
    private val videoProps: VideoDownloadProperties
) : CommonMessageHandler() {
    companion object {
        private val urlRegex = Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE)
    }

    @OptIn(PreviewFeature::class)
    override suspend fun handleMessage(message: CommonMessage<*>): Boolean {
        if (videoProps.enabled != true) {
            return false
        }
        val text = message.content.asTextContent()?.text ?: return false
        val url = urlRegex.find(text)?.value ?: return false

        val host = try {
            java.net.URI(url).host?.lowercase()
        } catch (_: Exception) {
            null
        } ?: return false

        val allowed = videoProps.allowedDomains.any { allowedDomain ->
            val d = allowedDomain.lowercase()
            host == d || host.endsWith("." + d)
        }
        if (!allowed) {
            return false
        }

        val downloaded = ytDlpService.downloadVideo(url)
        if (downloaded == null) {
            requestsExecutor.reply(message, "Не смог скачать тупое видео")
            return true
        }

        downloaded.use { dv ->
            requestsExecutor.withUploadVideoAction(message.chat) {
                requestsExecutor.sendVideo(
                    message.chat,
                    video = dv.file.asMultipartFile(),
                    text = "Вот твоё тупое видео",
                    replyToMessageId = message.messageId
                )
            }
        }

        return true
    }
}
