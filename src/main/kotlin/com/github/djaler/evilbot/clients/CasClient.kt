package com.github.djaler.evilbot.clients

import com.github.djaler.evilbot.components.RecordBreadcrumb
import io.ktor.client.*
import io.ktor.client.request.*
import org.springframework.stereotype.Component

@Component
@RecordBreadcrumb
class CasClient(
    private val httpClient: HttpClient
) {
    suspend fun getCasInfo(userId: Long): CasInfo {
        return httpClient.get("https://api.cas.chat/check?user_id=$userId")
    }
}

data class CasInfo(
    val result: CasResult?
)

data class CasResult(
    val messages: List<String>
)
