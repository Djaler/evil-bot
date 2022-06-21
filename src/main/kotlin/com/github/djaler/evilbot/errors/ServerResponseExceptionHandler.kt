package com.github.djaler.evilbot.errors

import com.github.djaler.evilbot.clients.SentryClient
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import org.springframework.stereotype.Component

@Component
class ServerResponseExceptionHandler(
    private val sentryClient: SentryClient
) : ExceptionHandler {
    override suspend fun handleException(e: Exception) {
        if (e !is ServerResponseException) {
            return
        }

        val response: String = e.response.bodyAsText()
        sentryClient.setExtra("response", response)
    }
}
