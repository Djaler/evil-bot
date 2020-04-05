package com.github.djaler.evilbot.components

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.springframework.stereotype.Component

@Component
@RecordBreadcrumb
class CasClient(
    private val httpClient: HttpClient
) {
    suspend fun getCasInfo(userId: Int): CasInfo {
        return httpClient.get("https://api.cas.chat/check?user_id=$userId")
    }
}

data class CasInfo(
    val result: CasResult?
)

data class CasResult(
    val messages: List<String>
)
