package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.requests.abstracts.MultipartFile
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import io.ktor.client.statement.*
import io.ktor.utils.io.streams.*
import org.springframework.core.io.ClassPathResource

fun ClassPathResource.asMultipartFile() = MultipartFile(
    filename ?: throw IllegalArgumentException("Incorrect resource provided: $this"),
    inputSource = { inputStream.asInput() }
)

suspend fun HttpResponse.asMultipartFile(filename: String) = bodyAsChannel().asMultipartFile(filename)
