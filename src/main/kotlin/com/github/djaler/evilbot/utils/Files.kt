package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.utils.StorageFile
import dev.inmo.tgbotapi.utils.StorageFileInfo
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.utils.io.streams.*
import org.springframework.core.io.ClassPathResource

fun StorageFile(resource: ClassPathResource): StorageFile {
    val fileName: String =
        resource.filename ?: throw IllegalArgumentException("Incorrect resource provided: $resource")

    return StorageFile(
        StorageFileInfo(
            fileName
        )
    ) {
        resource.inputStream.asInput()
    }
}

suspend fun StorageFile(httpResponse: HttpResponse, fileName:String): StorageFile {
    val bytes = httpResponse.receive<ByteArray>()

    return StorageFile(
        fileName,
        bytes,
    )
}
