package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.CurrencyService
import com.github.djaler.evilbot.service.UnknownCurrencyException
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.ParseMode.HTML
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import org.springframework.stereotype.Component
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@Component
class CurrencyConverterHandler(
    private val currencyService: CurrencyService,
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
        val originalAmount = amount.toBigDecimal()
        val convertedAmount = try {
            currencyService.convertCurrency(originalAmount, from, to)
        } catch (e: UnknownCurrencyException) {
            requestsExecutor.reply(message, "Не знаю про ${e.currency}")
            return
        }
        val decimalFormatSymbols = DecimalFormatSymbols(Locale.forLanguageTag("ru"))
        val decimalFormat = DecimalFormat("#,###.00", decimalFormatSymbols)
        val messageAmount = decimalFormat.format(originalAmount.setScale(2, RoundingMode.HALF_UP))
        val messageResult = decimalFormat.format(convertedAmount.setScale(2, RoundingMode.HALF_UP))
        requestsExecutor.reply(
            message,
            "Твои жалкие $messageAmount ${from.toUpperCase()} равны $messageResult ${to.toUpperCase()}"
        )
    }
}
