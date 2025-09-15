package com.github.djaler.evilbot.service

import org.springframework.stereotype.Service
import java.io.File

@Service
class VideoConvertService {

    fun convertToMp4(input: File, output: File) {
        // Требуется установленный ffmpeg в PATH (в Dockerfile он устанавливается)
        val command = listOf(
            "ffmpeg",
            "-hide_banner",
            "-loglevel", "error",
            "-y",
            "-i", input.absolutePath,
            "-movflags", "+faststart",
            "-pix_fmt", "yuv420p",
            "-c:v", "libx264",
            "-c:a", "aac",
            output.absolutePath
        )

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val code = process.waitFor()
        if (code != 0) {
            throw IllegalStateException("ffmpeg failed with exit code $code")
        }
    }
}
