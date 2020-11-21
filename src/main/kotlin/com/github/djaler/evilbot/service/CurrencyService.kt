package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.clients.FixerClient
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CurrencyService(
    private val fixerClient: FixerClient
) {
    @Throws(UnknownCurrencyException::class)
    suspend fun convertCurrency(amount: BigDecimal, from: String, to: String): BigDecimal {
        val (fromRate, toRate) = getRates(from, to)

        val maxScale = maxOf(amount.scale(), fromRate.scale(), toRate.scale())
        return amount.divide(fromRate, maxScale, RoundingMode.HALF_UP).multiply(toRate)
    }

    private suspend fun getRates(from: String, to: String): RatesPair {
        val rates = fixerClient.getLatestRates()

        val fromRate = rates[from.toUpperCase()] ?: throw UnknownCurrencyException(from)
        val toRate = rates[to.toUpperCase()] ?: throw UnknownCurrencyException(to)
        return RatesPair(fromRate, toRate)
    }
}

private data class RatesPair(val fromRate: BigDecimal, val toRate: BigDecimal)

class UnknownCurrencyException(val currency: String) : Exception()
