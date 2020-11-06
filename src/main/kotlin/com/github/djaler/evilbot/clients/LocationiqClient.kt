package com.github.djaler.evilbot.clients

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.djaler.evilbot.components.RecordBreadcrumb
import com.github.djaler.evilbot.config.LocationiqApiProperties
import io.github.resilience4j.kotlin.ratelimiter.RateLimiterConfig
import io.github.resilience4j.kotlin.ratelimiter.executeSuspendFunction
import io.github.resilience4j.ratelimiter.RateLimiter
import io.ktor.client.*
import io.ktor.client.request.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.io.Serializable
import java.time.Duration

@Component
@RecordBreadcrumb
class LocationiqClient(
    private val httpClient: HttpClient,
    private val locationiqApiProperties: LocationiqApiProperties
) {
    companion object {
        private const val baseUrl = "https://eu1.locationiq.com"
        private val rateLimiter = RateLimiter.of("Locationiq", RateLimiterConfig {
            limitForPeriod(2)
            limitRefreshPeriod(Duration.ofSeconds(1))
            timeoutDuration(Duration.ofSeconds(1))
        })
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
    data class Location(val displayName: String, val lat: Double, val lon: Double) : Serializable

    // TODO увеличить длительность кэширования
    @Cacheable("locations")
    suspend fun getLocation(query: String): List<Location> {
        return rateLimiter.executeSuspendFunction {
            httpClient.get("$baseUrl/v1/search.php") {
                parameter("key", locationiqApiProperties.key)
                parameter("format", "json")
                parameter("accept-language", "ru")
                parameter("q", query)
            }
        }
    }

    private data class TimezoneResponse(val timezone: Timezone)

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
    data class Timezone(val name: String, val shortName: String, val offsetSec: Long) : Serializable

    @Cacheable("locationTimezones")
    suspend fun getTimezone(latitude: Double, longitude: Double): Timezone {
        return rateLimiter.executeSuspendFunction {
            httpClient.get<TimezoneResponse>("$baseUrl/v1/timezone.php") {
                parameter("key", locationiqApiProperties.key)
                parameter("lat", latitude)
                parameter("lon", longitude)
            }.timezone
        }
    }
}
