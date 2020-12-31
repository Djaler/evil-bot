package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.utils.StorageFile
import dev.inmo.tgbotapi.utils.StorageFileInfo
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