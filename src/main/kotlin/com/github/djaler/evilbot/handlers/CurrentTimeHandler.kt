package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.LocationiqClient
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import io.ktor.client.features.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class CurrentTimeHandler(
    private val locationiqClient: LocationiqClient,
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

        val locations = try {
            locationiqClient.getLocation(query)
        } catch (e: ClientRequestException) {
            requestsExecutor.reply(
                message,
                "Не знаю такого"
            )
            return
        }

        val location = locations.first()
        val locationTimezone = locationiqClient.getTimezone(location.lat, location.lon)
        val locationTime = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(locationTimezone.offsetSec)
        val formattedLocationTime = locationTime.format(dateTimeFormatter)
        requestsExecutor.reply(
            message,
            if (args.isNullOrBlank()) "Ты не уточнил, поэтому вот результат для дефолт-сити: $formattedLocationTime" else formattedLocationTime
        )
    }
}
