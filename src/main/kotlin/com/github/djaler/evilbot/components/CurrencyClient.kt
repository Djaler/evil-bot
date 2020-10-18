package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.config.CurrencyProperties
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@RecordBreadcrumb
class CurrencyClient(
    private val httpClient: HttpClient,
    private val currencyProperties: CurrencyProperties
) {
    suspend fun getCurrencyInfo(currencyFrom: String, currencyTo: String): BigDecimal? {
        val result: Map<String, BigDecimal> = httpClient.get("https://free.currconv.com/api/v7/convert?compact=ultra&apiKey=${currencyProperties.key}&q=${currencyFrom}_${currencyTo}")
        return result["${currencyFrom}_${currencyTo}"]
    }
}