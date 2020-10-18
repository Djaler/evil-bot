package com.github.djaler.evilbot.handlers

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import com.github.djaler.evilbot.components.CurrencyClient
import dev.inmo.tgbotapi.types.ParseMode.HTML
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@Component
class CurrencyConverterHandler(
    private val currencyClient: CurrencyClient,
    private val requestsExecutor: RequestsExecutor,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("currency", "cur"),
    commandDescription = "Перевод валют"
) {
    companion object {
        private val WRONG_MESSAGES = "Пришли мне в таком виде: /currency <amount> <from> <to>"
        private val parseMode = HTML
    }

    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        if (args === null) {
            requestsExecutor.reply(message, WRONG_MESSAGES)
            return
        }
        val regex = """(?<amount>\d+?\.?\d+)\s(?<from>[A-z]{3})\s(?<to>[A-z]{3})""".toRegex()
        val currencyMessage = regex.find(args)
        if (currencyMessage === null) {
            requestsExecutor.reply(message, WRONG_MESSAGES)
            return
        }
        val (amount, from, to) = currencyMessage.destructured
        val moneyAmount = amount.toBigDecimal()
        val amountResult = currencyClient.getCurrencyInfo(from.toUpperCase(), to.toUpperCase())
        if (amountResult === null) {
            requestsExecutor.reply(message, "Такой валюты нет")
            return
        }
        val currencyResult = moneyAmount * amountResult
        val decimalFormatSymbols = DecimalFormatSymbols(Locale.CANADA_FRENCH)
        val decimalFormat = DecimalFormat("#,###.000", decimalFormatSymbols)
        val messageAmount = decimalFormat.format(moneyAmount.setScale(2, RoundingMode.HALF_DOWN))
        val messageResult = decimalFormat.format(currencyResult.setScale(2, RoundingMode.HALF_DOWN))
        requestsExecutor.reply(message, "Твои жалкие $messageAmount ${from.toUpperCase()} равны $messageResult ${to.toUpperCase()}")
    }
}