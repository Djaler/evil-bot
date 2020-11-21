package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.clients.FixerClient
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class CurrencyService(
    private val fixerClient: FixerClient
) {
    @Throws(UnknownCurrencyException::class)
    suspend fun convertCurrency(amount: BigDecimal, from: String, to: String): ConvertCurrencyResult {
        val (fromRate, toRate) = getRates(from, to)
        val (fromRateDayAgo, toRateDayAgo) = getRates(from, to, LocalDate.now(ZoneOffset.UTC).minusDays(1))

        return ConvertCurrencyResult(
            convertCurrency(amount, fromRate, toRate),
            convertCurrency(amount, fromRateDayAgo, toRateDayAgo)
        )
    }

    private fun convertCurrency(amount: BigDecimal, fromRate: BigDecimal, toRate: BigDecimal): BigDecimal {
        val maxScale = maxOf(amount.scale(), fromRate.scale(), toRate.scale())
        return amount.divide(fromRate, maxScale, RoundingMode.HALF_UP).multiply(toRate)
    }

    private suspend fun getRates(from: String, to: String, date: LocalDate? = null): RatesPair {
        val rates = if (date == null) fixerClient.getLatestRates() else fixerClient.getHistoricalRates(date)

        val fromRate = rates[from.toUpperCase()] ?: throw UnknownCurrencyException(from)
        val toRate = rates[to.toUpperCase()] ?: throw UnknownCurrencyException(to)
        return RatesPair(fromRate, toRate)
    }
}

private data class RatesPair(val fromRate: BigDecimal, val toRate: BigDecimal)

data class ConvertCurrencyResult(val convertedAmount: BigDecimal, val convertedAmountDayAgo: BigDecimal)

class UnknownCurrencyException(val currency: String) : Exception()
