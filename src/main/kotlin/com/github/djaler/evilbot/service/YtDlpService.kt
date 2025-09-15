package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.config.VideoDownloadProperties
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class YtDlpService(
    private val props: VideoDownloadProperties
) {
    companion object {
        private val log = LogManager.getLogger()
        private const val DEFAULT_TIMEOUT_MS = 5 * 60 * 1000L // 5 минут
    }

    class DownloadedVideo(
        val file: File,
        private val workDir: File
    ) : AutoCloseable {
        override fun close() {
            try { if (file.exists()) file.delete() } catch (_: Exception) {}
            try { workDir.deleteRecursively() } catch (_: Exception) {}
        }
    }

    fun downloadVideo(url: String, maxFileSize: String = props.maxFilesize): DownloadedVideo? {
        val workDir = Files.createTempDirectory("yt-dlp-" + UUID.randomUUID()).toFile()
        try {
            val command = listOf(
                "yt-dlp",
                "-q",
                "--no-progress",
                "--no-playlist",
                "--no-warnings",
                "--restrict-filenames",
                "--merge-output-format", "mp4",
                "--recode-video", "mp4",
                "--max-filesize", maxFileSize,
                "--print", "after_move:filepath",
                "-o", "%(id)s.%(ext)s",
                url
            )

            val process = ProcessBuilder(command)
                .directory(workDir)
                .redirectErrorStream(true)
                .start()

            val output = StringBuilder()
            process.inputStream.bufferedReader().use { r ->
                r.forEachLine { line -> output.appendLine(line) }
            }

            val finished = process.waitFor(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            if (!finished) {
                process.destroyForcibly()
                log.warn("yt-dlp timeout after {} ms. Output: {}", DEFAULT_TIMEOUT_MS, output.toString())
                return null
            }

            val exitCode = process.exitValue()
            if (exitCode != 0) {
                log.warn("yt-dlp exited with code {}. Output: {}", exitCode, output.toString())
                return null
            }

            val pathLine = output.lines().lastOrNull { it.isNotBlank() }?.trim()
            if (pathLine.isNullOrBlank()) {
                log.warn("yt-dlp did not print resulting filepath. Output: {}", output.toString())
                return null
            }

            val file = File(pathLine).let { if (it.isAbsolute) it else File(workDir, pathLine) }
            if (!file.exists() || file.length() == 0L) {
                log.warn("yt-dlp returned path but file missing/empty: {}. Output: {}", file.absolutePath, output.toString())
                return null
            }

            return DownloadedVideo(file, workDir)
        } catch (e: Exception) {
            log.error("Error while downloading via yt-dlp", e)
            return null
        }
    }
}
