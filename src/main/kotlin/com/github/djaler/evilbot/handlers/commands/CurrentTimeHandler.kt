package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.components.TelegramMediaSender
import com.github.djaler.evilbot.config.locationiq.LocationiqApiCondition
import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.service.TimeService
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asLocationContent
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.content.TextMessage
import org.springframework.context.annotation.Conditional
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
@Conditional(LocationiqApiCondition::class)
class CurrentTimeHandler(
    private val timeService: TimeService,
    private val requestsExecutor: RequestsExecutor,
    private val telegramMediaSender: TelegramMediaSender,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("time"),
    commandDescription = "Текущее время"
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        private val noonImage = ClassPathResource("media/high_noon.jpg")
    }

    override suspend fun handleCommand(
        message: TextMessage,
        args: String?
    ) {
        val locationContent = message.replyTo?.asContentMessage()?.content?.asLocationContent()

        var defaultLocationChosen = false

        val timeForLocation = if (locationContent !== null) {
            val location = locationContent.location
            timeService.getTimeForLocation(location.latitude, location.longitude)
        } else if (!args.isNullOrBlank()) {
            timeService.getTimeForLocation(args)
        } else {
            defaultLocationChosen = true
            timeService.getTimeForLocation("Москва")
        }

        if (timeForLocation === null) {
            requestsExecutor.reply(message, "Не знаю такого")
            return
        }

        if (isNoon(timeForLocation)) {
            telegramMediaSender.sendPhoto(message.chat.id, noonImage, replyTo = message.messageId)
            return
        }

        val formattedLocationTime = timeForLocation.format(dateTimeFormatter)

        requestsExecutor.reply(
            message,
            if (defaultLocationChosen) {
                "Ты не уточнил, поэтому вот результат для дефолт-сити: $formattedLocationTime"
            } else {
                formattedLocationTime
            }
        )
    }

    private fun isNoon(timeForLocation: LocalDateTime) = timeForLocation.hour == 12 && timeForLocation.minute == 0
}
