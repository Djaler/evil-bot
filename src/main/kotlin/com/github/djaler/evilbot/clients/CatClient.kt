package com.github.djaler.evilbot.clients

import com.github.djaler.evilbot.components.RecordBreadcrumb
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.springframework.stereotype.Component

@Component
@RecordBreadcrumb
class CatClient(
    private val httpClient: HttpClient
) {
    suspend fun getCat(): HttpResponse {
        return httpClient.get("https://thiscatdoesnotexist.com/")
    }
}