package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.config.FixerApiProperties
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@RecordBreadcrumb
class FixerClient(
    private val httpClient: HttpClient,
    private val fixerApiProperties: FixerApiProperties
) {
    @Cacheable("fixerLatestRates")
    fun getLatestRatesAsync(): Deferred<Map<String, BigDecimal>> {
        return GlobalScope.async {
            httpClient.get<LatestRates>("http://data.fixer.io/api/latest") {
                parameter("access_key", fixerApiProperties.key)
            }.rates
        }
    }
}

data class LatestRates(val rates: Map<String, BigDecimal>)
