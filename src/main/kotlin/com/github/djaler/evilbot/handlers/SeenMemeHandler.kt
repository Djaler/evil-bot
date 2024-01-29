package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.base.MessageHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.DuplicateImageChecker
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asPhotoContent
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.formatting.makeLinkToMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.link
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@Component
class SeenMemeHandler(
    private val requestExecutor: RequestsExecutor,
    private val duplicateImageChecker: DuplicateImageChecker,
    private val chatService: ChatService,
) : MessageHandler() {
    override suspend fun handleMessage(message: Message): Boolean {
        val chat = message.chat.asPublicChat() ?: return false
        val imageFile = message.asContentMessage()?.content?.asPhotoContent()?.media ?: return false

        val photoBytes = requestExecutor.downloadFile(imageFile)
        val image = ByteArrayInputStream(photoBytes).use {
            ImageIO.read(it)
        }

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
        val originalMessageId: Long? = duplicateImageChecker.findDuplicate(image, chatEntity)

        if (originalMessageId == null) {
            duplicateImageChecker.saveHash(image, chatEntity, message.messageId, imageFile.fileId)
            return false
        } else {
            val messageLink = makeLinkToMessage(message.chat, originalMessageId) ?: return false

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
