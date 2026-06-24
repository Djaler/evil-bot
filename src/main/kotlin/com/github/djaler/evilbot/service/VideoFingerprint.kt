package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.clients.SentryClient
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.requests.abstracts.FileId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

internal const val FRAME_MAX_HAMMING_DISTANCE = 5

internal fun hammingDistance(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)

/**
 * Покадровое (по индексу) сравнение векторов dHash на общем префиксе.
 * Дубликат, если совпало строгое большинство кадров (для 4 кадров — ≥ 3).
 */
internal fun framesMatch(a: List<Long>, b: List<Long>, maxDistance: Int = FRAME_MAX_HAMMING_DISTANCE): Boolean {
    val n = minOf(a.size, b.size)
    if (n == 0) return false
    val matching = (0 until n).count { hammingDistance(a[it], b[it]) <= maxDistance }
    return matching * 2 > n
}

/**
 * Относительные позиции кадров в секундах. При известной длительности — доли от неё,
 * иначе фиксированный фолбэк (лишние кадры просто не извлекутся ffmpeg-ом).
 */
internal fun framePositionsSeconds(
    durationSeconds: Long?,
    fractions: List<Double>,
    fallback: List<Double>
): List<Double> =
    if (durationSeconds != null && durationSeconds > 0) {
        fractions.map { (it * durationSeconds * 1000).roundToInt() / 1000.0 }
    } else {
        fallback
    }

@Service
class VideoFingerprintService(
    private val requestExecutor: RequestsExecutor,
    private val sentryClient: SentryClient,
) {
    companion object {
        private val log = LogManager.getLogger()
        private const val MAX_DOWNLOAD_BYTES = 20L * 1024 * 1024
        private val FRAME_FRACTIONS = listOf(0.2, 0.4, 0.6, 0.8)
        private val FALLBACK_SECONDS = listOf(1.0, 2.0, 3.0, 4.0)
        private const val MIN_FRAMES = 2
        private const val FFMPEG_TIMEOUT_MS = 30_000L
    }

    /**
     * Вектор dHash ключевых кадров видео. null — если глубокую проверку выполнить нельзя
     * (файл > 20 МБ, ошибка скачивания/ffmpeg/декодирования или извлеклось < 2 кадров):
     * вызывающий трактует null как «проверка невозможна» и молчит.
     */
    suspend fun extractFrameHashes(fileId: FileId, fileSize: Long?, durationSeconds: Long?): List<Long>? {
        if (fileSize != null && fileSize > MAX_DOWNLOAD_BYTES) {
            return null
        }
        val positions = framePositionsSeconds(durationSeconds, FRAME_FRACTIONS, FALLBACK_SECONDS)
        return try {
            val bytes = requestExecutor.downloadFile(fileId)
            withContext(Dispatchers.IO) {
                val input = Files.createTempFile("dedup-", ".mp4").toFile()
                try {
                    input.writeBytes(bytes)
                    extractFrames(input, positions).takeIf { it.size >= MIN_FRAMES }
                } finally {
                    input.delete()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn("Failed to extract video frames, skipping deep duplicate check", e)
            sentryClient.captureException(e)
            null
        }
    }

    /**
     * Один вызов ffmpeg на все позиции: отдельный input-seek (`-ss` перед `-i`) на каждую,
     * затем dHash тех PNG, что реально создались, в порядке позиций. Частичный успех допустим —
     * полагаемся на наличие выходных файлов, а не на общий exit code.
     */
    private fun extractFrames(input: File, positions: List<Double>): List<Long> {
        val outputs = positions.map { Files.createTempFile("dedup-frame-", ".png").toFile() }
        return try {
            val command = buildList {
                addAll(listOf("ffmpeg", "-hide_banner", "-loglevel", "error", "-y"))
                positions.forEach { second -> addAll(listOf("-ss", second.toString(), "-i", input.absolutePath)) }
                outputs.forEachIndexed { index, output ->
                    addAll(listOf("-map", "$index:v", "-frames:v", "1", output.absolutePath))
                }
            }
            val process = ProcessBuilder(command)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            if (!process.waitFor(FFMPEG_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly()
                return emptyList()
            }
            outputs.mapNotNull { output ->
                if (output.length() == 0L) null else ImageIO.read(output)?.let(::dHash)
            }
        } finally {
            outputs.forEach { it.delete() }
        }
    }
}
