package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.MediaHash
import com.github.djaler.evilbot.repository.MediaHashRepository
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.MessageId
import org.springframework.stereotype.Component
import java.awt.Image
import java.awt.image.BufferedImage

@Component
class DuplicateMediaChecker(
    private val mediaHashRepository: MediaHashRepository,
) {
    companion object {
        private const val MAX_HAMMING_DISTANCE = 5
    }

    fun findDuplicate(image: BufferedImage, chat: Chat): Long? {
        val duplicate = mediaHashRepository.findByChatIdAndHashCloseTo(chat.id, dHash(image), MAX_HAMMING_DISTANCE)

        return duplicate?.messageId
    }

    fun saveHash(image: BufferedImage, chat: Chat, messageId: MessageId, fileId: FileId) {
        mediaHashRepository.save(
            MediaHash(
                dHash(image),
                chat.id,
                messageId.long,
                fileId.fileId
            )
        )
    }
}

/**
 * Перцептивный difference hash: 9x8 в оттенках серого, бит = «левый пиксель ярче правого».
 * Уменьшенная копия всегда рисуется в новый BufferedImage типа TYPE_INT_RGB (а не image.type):
 * у CMYK-JPEG тип TYPE_CUSTOM, и конструктор BufferedImage на нём падает.
 */
internal fun dHash(image: BufferedImage): Long {
    val scaled = image.getScaledInstance(9, 8, Image.SCALE_SMOOTH)
    val resized = BufferedImage(9, 8, BufferedImage.TYPE_INT_RGB)
    val graphics = resized.createGraphics()
    check(graphics.drawImage(scaled, 0, 0, null)) { "scaled image was not rendered synchronously" }
    graphics.dispose()

    var hash = 0L
    for (y in 0 until 8) {
        for (x in 0 until 8) {
            if (gray(resized.getRGB(x, y)) > gray(resized.getRGB(x + 1, y))) {
                hash = hash or (1L shl (y * 8 + x))
            }
        }
    }
    return hash
}

private fun gray(rgb: Int): Int {
    val r = rgb shr 16 and 0xFF
    val g = rgb shr 8 and 0xFF
    val b = rgb and 0xFF
    return (r * 299 + g * 587 + b * 114) / 1000
}
