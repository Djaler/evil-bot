package com.github.djaler.evilbot.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.Json
import io.ktor.http.ContentType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestConfig {
    @Bean
    fun httpClient(objectMapper: ObjectMapper): HttpClient {
        return HttpClient {
            Json {
                serializer = JacksonSerializer(objectMapper)

                acceptContentTypes = listOf(ContentType.Any)
                //TODO заменить на accept после исправления бага https://github.com/ktorio/ktor/issues/1765
            }
        }
    }
}
