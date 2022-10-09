package com.github.djaler.evilbot.clients

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.djaler.evilbot.components.RecordBreadcrumb
import com.github.djaler.evilbot.config.locationiq.LocationiqApiCondition
import com.github.djaler.evilbot.config.locationiq.LocationiqApiProperties
import com.github.djaler.evilbot.utils.cached
import com.github.djaler.evilbot.utils.getCacheOrThrow
import io.github.resilience4j.kotlin.ratelimiter.RateLimiterConfig
import io.github.resilience4j.kotlin.ratelimiter.executeSuspendFunction
import io.github.resilience4j.ratelimiter.RateLimiter
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import java.io.Serializable
import java.time.Duration

@Component
@Conditional(LocationiqApiCondition::class)
@RecordBreadcrumb
class LocationiqClient(
    private val httpClient: HttpClient,
    private val locationiqApiProperties: LocationiqApiProperties,
    private val cacheManager: CacheManager
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

    suspend fun getLocation(query: String): List<Location> {
        val cache = cacheManager.getCacheOrThrow("locations")

        return cached(cache, query) {
            rateLimiter.executeSuspendFunction {
                httpClient.get("$baseUrl/v1/search.php") {
                    parameter("key", locationiqApiProperties.key)
                    parameter("format", "json")
                    parameter("accept-language", "ru")
                    parameter("q", query)
                }.body()
            }
        }
    }

    private data class TimezoneResponse(val timezone: Timezone)

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
    data class Timezone(val name: String, val shortName: String, val offsetSec: Long) : Serializable

    suspend fun getTimezone(latitude: Double, longitude: Double): Timezone {
        val cache = cacheManager.getCacheOrThrow("locationTimezones")

        return cached(cache, listOf(latitude, longitude)) {
            rateLimiter.executeSuspendFunction {
                httpClient.get("$baseUrl/v1/timezone.php") {
                    parameter("key", locationiqApiProperties.key)
                    parameter("lat", latitude)
                    parameter("lon", longitude)
                }.body<TimezoneResponse>().timezone
            }
        }
    }
}
