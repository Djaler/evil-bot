package com.github.djaler.evilbot.handlers

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
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
    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        if (args === null) {
            requestsExecutor.sendMessage(message.chat, "Ну а где варианты? Пришли варианты, разделенные слэшом (/)", replyToMessageId = message.messageId)
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

        requestsExecutor.sendMessage(message.chat, answerText, replyToMessageId = message.messageId)
    }
}
