package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.components.FixerClient
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CurrencyService(
    private val fixerClient: FixerClient
) {
    @Throws(UnknownCurrencyException::class)
    suspend fun convertCurrency(amount: BigDecimal, from: String, to: String): BigDecimal {
        val latestRates = fixerClient.getLatestRates()

        val fromRate = latestRates[from.toUpperCase()] ?: throw UnknownCurrencyException(from)
        val toRate = latestRates[to.toUpperCase()] ?: throw UnknownCurrencyException(to)

        return amount / fromRate * toRate
    }
}

class UnknownCurrencyException(val currency: String) : Exception()
