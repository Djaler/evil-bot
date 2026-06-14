package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.handlers.base.MessageHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.DuplicateMediaChecker
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.formatting.makeLinkToMessage
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.message.abstracts.AccessibleMessage
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.types.message.content.VideoContent
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.link
import kotlinx.coroutines.CancellationException
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@Component
class SeenMemeHandler(
    private val requestExecutor: RequestsExecutor,
    private val duplicateMediaChecker: DuplicateMediaChecker,
    private val chatService: ChatService,
    private val sentryClient: SentryClient,
) : MessageHandler() {
    companion object {
        private val log = LogManager.getLogger()
    }

    override suspend fun handleMessage(message: AccessibleMessage): Boolean {
        val chat = message.chat.asPublicChat() ?: return false
        val content = message.asContentMessage()?.content ?: return false

        // для хеша 9x8 хватает мини-превью: самое маленькое фото или thumbnail видео
        val preview = when (content) {
            is PhotoContent -> content.mediaCollection.minByOrNull { it.resolution }
            is VideoContent -> content.media.thumbnail
            else -> null
        } ?: return false

        val image = try {
            val bytes = requestExecutor.downloadFile(preview)
            ByteArrayInputStream(bytes).use { ImageIO.read(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn("Failed to download or decode media, skipping duplicate check", e)
            sentryClient.captureException(e)
            return false
        } ?: return false

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
        val originalMessageId = duplicateMediaChecker.findDuplicate(image, chatEntity)

        if (originalMessageId == null) {
            // для видео это fileId превью, а не самого медиа
            duplicateMediaChecker.saveHash(image, chatEntity, message.messageId, preview.fileId)
            return false
        } else {
            val messageLink = makeLinkToMessage(message.chat, MessageId(originalMessageId)) ?: return false

            requestExecutor.reply(
                message,
                buildEntities {
                    +"Уже было - " + link(messageLink)
                }
            )
            return true
        }
    }
}
