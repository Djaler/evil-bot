package com.github.djaler.evilbot.clients

import com.github.djaler.evilbot.components.RecordBreadcrumb
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.springframework.stereotype.Component

@Component
@RecordBreadcrumb
class SberClient(
    private val httpClient: HttpClient
) {

    private data class PredictRequest(val text: String)
    private data class PredictResponse(val predictions: String)

    suspend fun getPrediction(text: String): String {
        return httpClient.post {
            url("https://api.aicloud.sbercloud.ru/public/v1/public_inference/gpt3/predict")

            contentType(ContentType.Application.Json)
            setBody(PredictRequest(text))

            headers {
                append(HttpHeaders.Origin, "https://russiannlp.github.io")
            }

            timeout {
                socketTimeoutMillis = 10 * 60 * 1000
            }
        }.body<PredictResponse>().predictions
    }
}
