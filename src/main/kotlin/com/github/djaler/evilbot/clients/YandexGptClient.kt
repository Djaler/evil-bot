package com.github.djaler.evilbot.clients

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.djaler.evilbot.components.RecordBreadcrumb
import com.github.djaler.evilbot.config.yandex.YandexApiCondition
import com.github.djaler.evilbot.config.yandex.YandexApiProperties
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
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
            setBody(mapOf(
                "token" to token
            ))
        }.body()
    }
}

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class SharingUrlResult(
    val sharingUrl: String
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class SummaryResult(
    val thesis: List<Thesis>
)

data class Thesis(
    var content: String
)
