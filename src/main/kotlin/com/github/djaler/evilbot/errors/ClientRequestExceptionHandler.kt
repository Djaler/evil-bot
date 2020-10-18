package com.github.djaler.evilbot.errors

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.sentry.SentryClient
import org.springframework.stereotype.Component

@Component
class ClientRequestExceptionHandler(
    private val sentryClient: SentryClient
) : ExceptionHandler {
    override suspend fun handleException(e: Exception) {
        if (e !is ClientRequestException) {
            return
        }

        val response: String = e.response.receive()
        sentryClient.context.addExtra("response", response)
    }
}
