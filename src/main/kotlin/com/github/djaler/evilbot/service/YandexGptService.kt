package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.clients.YandexGptClient
import org.springframework.stereotype.Service

@Service
class YandexGptService(
    private val yandexGptClient: YandexGptClient
) {
    suspend fun generateLinkThesis(link: String): String? {
        val result = yandexGptClient.generateLinkSummary(link)

        val generatedResult = yandexGptClient.getGeneratedResult(result.sharingUrl)

        return generatedResult.thesis.joinToString(separator = "\n") { it.content }
    }
}
