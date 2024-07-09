package com.github.djaler.evilbot.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.sentry.Breadcrumb
import io.sentry.Sentry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestConfig {
    @Bean
    fun httpClient(objectMapper: ObjectMapper): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                register(ContentType.Any, JacksonConverter(objectMapper))
            }

            install(HttpTimeout)

            install(Logging) {
                level = LogLevel.ALL
            }

            install(SentryPlugin)
        }
    }
}

val SentryPlugin = createClientPlugin("SentryPlugin") {
    onRequest { request, content ->
        Sentry.addBreadcrumb(
            Breadcrumb.http(
                request.url.toString(),
                request.method.value
            ).apply {
                setData("content", content)
            }
        )
    }

    val responseObserver = ResponseObserver({ response ->
        val request = response.request
        Sentry.addBreadcrumb(
            Breadcrumb.http(
                request.url.toString(),
                request.method.value
            ).apply {
                setData("content", response.bodyAsText())
            }
        )
    })
    ResponseObserver.install(responseObserver, client)
}
