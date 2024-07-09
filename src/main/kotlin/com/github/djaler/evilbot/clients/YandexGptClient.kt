package com.github.djaler.evilbot.clients

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.djaler.evilbot.components.RecordBreadcrumb
import com.github.djaler.evilbot.config.yandex.YandexApiCondition
import com.github.djaler.evilbot.config.yandex.YandexApiProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(YandexApiCondition::class)
@RecordBreadcrumb
class YandexGptClient(
    private val httpClient: HttpClient,
    private val yandexApiProperties: YandexApiProperties
) {
    suspend fun generateLinkSummary(link: String): SharingUrlResult {
        return httpClient.post {
            url("https://300.ya.ru/api/sharing-url")

            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "article_url" to link
            ))

            headers {
                append(HttpHeaders.Authorization, "OAuth ${yandexApiProperties.token}")
            }
        }.body()
    }

    suspend fun getGeneratedResult(sharingUrl: String): SummaryResult {
        val token = sharingUrl.split('/').last()

        return httpClient.post {
            url("https://300.ya.ru/api/sharing")

            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "token" to token
                )
            )
        }.body()
    }

    suspend fun generateVideoSummary(
        link: String,
        sessionId: String? = null,
    ): VideoSummaryResult {
        return httpClient.post {
            url("https://300.ya.ru/api/generation")

            contentType(ContentType.Application.Json)
            cookie("Session_id", yandexApiProperties.cookie)
            setBody(
                mapOf(
                    "video_url" to link,
                    "session_id" to sessionId,
                )
            )
        }.body()
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SharingUrlResult(
    val sharingUrl: String
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SummaryResult(
    val thesis: List<Thesis>
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class VideoSummaryResult(
    val statusCode: Int,
    val errorCode: Int?,
    val message: String?,
    val sessionId: String?,
    val pollIntervalMs: Long,
    val keypoints: List<Keypoint>?
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Keypoint(
    val content: String,
    val startTime: Long,
    val theses: List<Thesis>,
)

data class Thesis(
    var content: String
)

object ResponseStatus {
    const val SUCCESS_VIDEO = 0
    const val IN_PROGRESS = 1
    const val ERROR = 3
}
