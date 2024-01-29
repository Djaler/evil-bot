package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.ImageHash
import com.github.djaler.evilbot.repository.ImageHashRepository
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.MessageIdentifier
import korlibs.crypto.encoding.hex
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import javax.imageio.ImageIO

@Component
class DuplicateImageChecker(
    private val imageHashRepository: ImageHashRepository,
) {
    fun findDuplicate(image: BufferedImage, chat: Chat): MessageIdentifier? {
        val hash = resizeAndGetHash(image)

        val duplicate = imageHashRepository.findByChatIdAndHash(chat.id, hash)

        return duplicate?.messageId
    }

    fun saveHash(image: BufferedImage, chat: Chat, messageId: MessageIdentifier, fileId: FileId) {
        val hash = resizeAndGetHash(image)

        imageHashRepository.save(ImageHash(
            hash,
            chat.id,
            messageId,
            fileId.fileId
        ))
    }

    // TODO cache
    private fun resizeAndGetHash(image: BufferedImage): String {
        val resizedImage = resizeImage(image, width = 64, height = 64)

        return getImageHash(resizedImage)
    }

    private fun resizeImage(image: BufferedImage, width: Int, height: Int): BufferedImage {
        return BufferedImage(width, height, image.type).apply {
            graphics.drawImage(image, 0, 0, 64, 64, null)
        }
    }

    private fun getImageHash(image: BufferedImage): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        ByteArrayOutputStream().use { baos ->
            DigestOutputStream(baos, messageDigest).use { dos ->
                ImageIO.write(image, "png", dos)
                dos.flush()
            }
        }

        return messageDigest.digest().hex
    }
}
