package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.TimeService
import com.github.djaler.evilbot.utils.StorageFile
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.media.replyWithPhoto
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asLocationContent
import dev.inmo.tgbotapi.requests.abstracts.MultipartFile
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.time.LocalDateTime
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

        private val noonImage by lazy {
            MultipartFile(StorageFile(ClassPathResource("media/high_noon.jpg")))
        }
    }

    override suspend fun <M> handleCommand(
        message: M,
        args: String?
    ) where M : CommonMessage<TextContent>, M : FromUserMessage {
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
            requestsExecutor.replyWithPhoto(message, noonImage)
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
