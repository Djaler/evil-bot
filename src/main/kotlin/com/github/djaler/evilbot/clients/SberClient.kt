package com.github.djaler.evilbot.clients

import com.github.djaler.evilbot.components.RecordBreadcrumb
import io.ktor.client.*
import io.ktor.client.features.*
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
        return httpClient.post<PredictResponse> {
            url("https://api.aicloud.sbercloud.ru/public/v1/public_inference/gpt3/predict")

            contentType(ContentType.Application.Json)
            body = PredictRequest(text)

            headers {
                append(HttpHeaders.Origin, "https://russiannlp.github.io")
            }

            timeout {
                socketTimeoutMillis = 10 * 60 * 1000
            }
        }.predictions
    }
}
