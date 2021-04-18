package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.utils.StorageFile
import dev.inmo.tgbotapi.utils.StorageFileInfo
import dev.inmo.tgbotapi.utils.createMimeType
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.streams.*
import org.springframework.core.io.ClassPathResource
import javax.activation.MimetypesFileTypeMap

fun StorageFile(resource: ClassPathResource): StorageFile {
    val fileName: String =
        resource.filename ?: throw IllegalArgumentException("Incorrect resource provided: $resource")

    return StorageFile(
        StorageFileInfo(
            MimetypesFileTypeMap().getContentType(fileName),
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
        createMimeType(httpResponse.contentType()?.toString() ?: "application/octet-stream")
    )
}