package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.clients.Keypoint
import com.github.djaler.evilbot.clients.ResponseStatus
import com.github.djaler.evilbot.clients.VideoSummaryResult
import com.github.djaler.evilbot.clients.YandexGptClient
import com.github.djaler.evilbot.config.yandex.YandexApiCondition
import kotlinx.coroutines.delay
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service

@Service
@Conditional(YandexApiCondition::class)
class YandexGptService(
    private val yandexGptClient: YandexGptClient
) {
    suspend fun generateLinkThesis(link: String): String? {
        val result = yandexGptClient.generateLinkSummary(link)

        val generatedResult = yandexGptClient.getGeneratedResult(result.sharingUrl)

        return generatedResult.thesis.joinToString(separator = "\n") { it.content }
    }

    suspend fun generateVideoKeypoints(link: String): List<Keypoint> {
        var result = yandexGptClient.generateVideoSummary(link)

        while (result.statusCode == ResponseStatus.IN_PROGRESS) {
            delay(result.pollIntervalMs)

            result = yandexGptClient.generateVideoSummary(link, sessionId = result.sessionId)
        }

        return result.keypoints ?: throw VideoThesisGenerationException(link, result)
    }
}

class VideoThesisGenerationException(link: String, result: VideoSummaryResult) : RuntimeException(
    "Error on video generation for $link. Status: ${result.statusCode}, error: ${result.errorCode}, message: ${result.message}"
)
