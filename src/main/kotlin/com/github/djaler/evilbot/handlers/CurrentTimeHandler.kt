package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.TimeService
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class CurrentTimeHandler(
    private val timeService: TimeService,
    private val requestsExecutor: RequestsExecutor,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("time"),
    commandDescription = "Текущее время"
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val query = if (args.isNullOrBlank()) "Москва" else args

        val timeForLocation = timeService.getTimeForLocation(query)

        val text = if (timeForLocation !== null) {
            val formattedLocationTime = timeForLocation.format(dateTimeFormatter)
            if (args.isNullOrBlank()) "Ты не уточнил, поэтому вот результат для дефолт-сити: $formattedLocationTime" else formattedLocationTime
        } else {
            "Не знаю такого"
        }

        requestsExecutor.reply(
            message,
            text
        )
    }
}
