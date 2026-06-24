package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.MediaHash
import com.github.djaler.evilbot.repository.MediaHashRepository
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.MessageId
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.awt.Image
import java.awt.image.BufferedImage
import java.time.Instant

@Component
class DuplicateMediaChecker(
    private val mediaHashRepository: MediaHashRepository,
) {
    companion object {
        private const val MAX_HAMMING_DISTANCE = 5
        private const val DURATION_TOLERANCE_SECONDS = 1L
    }

    /** Ближайший по хешу кандидат в пределах допуска, либо null. */
    @Transactional(readOnly = true)
    fun findImageCandidate(chat: Chat, hash: Long): MediaHash? {
        return mediaHashRepository.findByChatIdAndHashCloseTo(chat.id, hash, MAX_HAMMING_DISTANCE)
    }

    @Transactional
    fun deleteOlderThan(threshold: Instant): Int {
        return mediaHashRepository.deleteByLastSeenAtBefore(threshold)
    }

    /**
     * Tier 0 для видео. С известной длительностью — гейт по hash + duration; без неё — только по hash
     * (эскалацию решает Tier 1).
     */
    @Transactional(readOnly = true)
    fun findVideoCandidates(chat: Chat, thumbHash: Long, durationSeconds: Long?): List<MediaHash> {
        if (durationSeconds != null) {
            return mediaHashRepository.findVideoCandidates(
                chat.id,
                thumbHash,
                MAX_HAMMING_DISTANCE,
                durationSeconds - DURATION_TOLERANCE_SECONDS,
                durationSeconds + DURATION_TOLERANCE_SECONDS
            )
        }
        // Hash-only fallback (LIMIT 1): returns at most one candidate, acceptable per spec —
        // the caller cannot compute a duration gate, so hash proximity alone is sufficient.
        return mediaHashRepository.findByChatIdAndHashCloseTo(chat.id, thumbHash, MAX_HAMMING_DISTANCE)
            ?.let(::listOf)
            ?: emptyList()
    }

    @Transactional
    fun record(
        hash: Long,
        chat: Chat,
        messageId: MessageId,
        fileId: FileId,
        durationSeconds: Long? = null,
        frameHashes: List<Long>? = null
    ) {
        mediaHashRepository.save(
            MediaHash(hash, chat.id, messageId.long, fileId.fileId, Instant.now(), durationSeconds, frameHashes)
        )
    }

    /** Сдвигает запись на текущее сообщение (продлевает TTL), возвращает предыдущий messageId. */
    @Transactional
    fun shiftToCurrent(existing: MediaHash, messageId: MessageId): Long {
        val previousMessageId = existing.messageId
        mediaHashRepository.save(existing.copy(messageId = messageId.long, lastSeenAt = Instant.now()))
        return previousMessageId
    }

    /** Ленивый кэш: до-сохраняет вектор кадров кандидату. */
    @Transactional
    fun cacheFrameHashes(existing: MediaHash, frameHashes: List<Long>) {
        mediaHashRepository.save(existing.copy(frameHashes = frameHashes))
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
