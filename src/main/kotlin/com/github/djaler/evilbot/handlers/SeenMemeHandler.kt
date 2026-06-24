package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.handlers.base.MessageHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.DuplicateMediaChecker
import com.github.djaler.evilbot.service.VideoFingerprintService
import com.github.djaler.evilbot.service.dHash
import com.github.djaler.evilbot.service.framesMatch
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.formatting.makeLinkToMessage
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.message.abstracts.AccessibleMessage
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.types.message.content.VideoContent
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.link
import kotlinx.coroutines.CancellationException
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@Component
class SeenMemeHandler(
    private val requestExecutor: RequestsExecutor,
    private val duplicateMediaChecker: DuplicateMediaChecker,
    private val videoFingerprintService: VideoFingerprintService,
    private val chatService: ChatService,
    private val sentryClient: SentryClient,
) : MessageHandler() {
    companion object {
        private val log = LogManager.getLogger()
    }

    override suspend fun handleMessage(message: AccessibleMessage): Boolean {
        val chat = message.chat.asPublicChat() ?: return false
        val content = message.asContentMessage()?.content ?: return false

        return when (content) {
            is PhotoContent -> {
                // для хеша 9x8 хватает самого маленького превью
                val preview = content.mediaCollection.minByOrNull { it.resolution } ?: return false
                val image = downloadImage(preview.fileId) ?: return false
                val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
                val previousMessageId =
                    duplicateMediaChecker.checkAndRecord(image, chatEntity, message.messageId, preview.fileId)
                        ?: return false
                replyAlreadySeen(message, previousMessageId)
            }
            is VideoContent -> {
                val thumbnail = content.media.thumbnail ?: return false
                val image = downloadImage(thumbnail.fileId) ?: return false
                val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
                handleVideo(message, content, chatEntity, dHash(image))
            }
            else -> false
        }
    }

    private suspend fun handleVideo(
        message: AccessibleMessage,
        content: VideoContent,
        chatEntity: com.github.djaler.evilbot.entity.Chat,
        thumbHash: Long
    ): Boolean {
        val media = content.media
        val candidates = duplicateMediaChecker.findVideoCandidates(chatEntity, thumbHash, media.duration)
        if (candidates.isEmpty()) {
            duplicateMediaChecker.recordVideo(thumbHash, chatEntity, message.messageId, media.fileId, media.duration, null)
            return false
        }

        // Tier 1: дорогая проверка только на коллизии Tier 0
        val newFrames = videoFingerprintService.extractFrameHashes(media.fileId, media.fileSize?.bytes?.toLong(), media.duration)
        if (newFrames == null) {
            // проверка невозможна (>20МБ / ошибка) → молчим
            duplicateMediaChecker.recordVideo(thumbHash, chatEntity, message.messageId, media.fileId, media.duration, null)
            return false
        }

        for (candidate in candidates) {
            val candidateFrames = candidate.frameHashes
                ?: videoFingerprintService.extractFrameHashes(FileId(candidate.fileId), null, candidate.duration)
                    ?.also { duplicateMediaChecker.cacheFrameHashes(candidate, it) }
                ?: continue
            if (framesMatch(newFrames, candidateFrames)) {
                val previousMessageId = duplicateMediaChecker.shiftToCurrent(candidate, message.messageId)
                return replyAlreadySeen(message, previousMessageId)
            }
        }

        // та же обложка+длительность, но другой контент → молчим, кэшируем кадры нового
        duplicateMediaChecker.recordVideo(thumbHash, chatEntity, message.messageId, media.fileId, media.duration, newFrames)
        return false
    }

    private suspend fun downloadImage(fileId: FileId): BufferedImage? {
        return try {
            val bytes = requestExecutor.downloadFile(fileId)
            ByteArrayInputStream(bytes).use { ImageIO.read(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn("Failed to download or decode media, skipping duplicate check", e)
            sentryClient.captureException(e)
            null
        }
    }

    private suspend fun replyAlreadySeen(message: AccessibleMessage, previousMessageId: Long): Boolean {
        val messageLink = makeLinkToMessage(message.chat, MessageId(previousMessageId)) ?: return false
        requestExecutor.reply(
            message,
            buildEntities {
                +"Уже было - " + link(messageLink)
            }
        )
        return true
    }
}
