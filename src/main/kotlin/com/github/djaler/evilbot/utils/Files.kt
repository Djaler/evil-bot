package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.requests.abstracts.MultipartFile
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import io.ktor.client.statement.*
import io.ktor.utils.io.streams.*
import org.springframework.core.io.ClassPathResource
import java.io.File

fun ClassPathResource.asMultipartFile() = MultipartFile(
    filename ?: throw IllegalArgumentException("Incorrect resource provided: $this"),
    inputSource = { inputStream.asInput() }
)

suspend fun HttpResponse.asMultipartFile(filename: String) = bodyAsChannel().asMultipartFile(filename)

fun ByteArray.toTemporaryFile(parent: File, name: String): TempFile {
    val file = TempFile(parent.resolve(name).path)
    file.writeBytes(this)
    return file
}

class TempFile(path: String) : File(path), AutoCloseable {
    override fun close() {
        delete()
    }
}
