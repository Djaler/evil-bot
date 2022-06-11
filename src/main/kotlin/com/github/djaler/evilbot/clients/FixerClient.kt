package com.github.djaler.evilbot.clients

import com.github.djaler.evilbot.components.RecordBreadcrumb
import com.github.djaler.evilbot.config.FixerApiProperties
import com.github.djaler.evilbot.utils.cached
import com.github.djaler.evilbot.utils.getCacheOrThrow
import io.ktor.client.*
import io.ktor.client.request.*
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
@RecordBreadcrumb
class FixerClient(
    private val httpClient: HttpClient,
    private val fixerApiProperties: FixerApiProperties,
    private val cacheManager: CacheManager
) {
    suspend fun getLatestRates(): Map<String, BigDecimal> {
        val cache = cacheManager.getCacheOrThrow("fixerLatestRates")

        return cached(cache, "rates") {
            httpClient.get<LatestRates>("http://data.fixer.io/api/latest") {
                parameter("access_key", fixerApiProperties.key)
            }.rates
        }
    }

    suspend fun getHistoricalRates(date: LocalDate): Map<String, BigDecimal> {
        val cache = cacheManager.getCacheOrThrow("fixerHistoricalRates")

        return cached(cache, date) {
            httpClient.get<HistoricalRates>("http://data.fixer.io/api/$date") {
                parameter("access_key", fixerApiProperties.key)
            }.rates
        }
    }
}

data class LatestRates(val rates: Map<String, BigDecimal>)
data class HistoricalRates(val rates: Map<String, BigDecimal>)
