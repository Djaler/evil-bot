package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.handlers.base.CommandHandler
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class ResolveHandler(
    private val requestsExecutor: RequestsExecutor,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("resolve", "r"),
    commandDescription = "выбрать один из вариантов"
) {
    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        if (args === null) {
            requestsExecutor.reply(message, "Ну а где варианты? Пришли варианты, разделенные слэшом (/)")
            return
        }

        val variants = args
            .split(Regex(" */ *"))
            .distinct()
            .filter { it.isNotEmpty() }

        val answerText = when {
            variants.isEmpty() -> "Ну а где варианты?"
            variants.size == 1 -> "Эмм, тут так-то один вариант..."
            Random.nextInt(0, 100) == 0 -> "Я откуда знаю? Отъебись"
            else -> variants.random()
        }

        requestsExecutor.reply(message, answerText)
    }
}
