package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class ResolveHandler(
    private val telegramClient: TelegramClient,
    botInfo: ExtendedBot
) : CommandHandler(botInfo, command = arrayOf("r", "resolve")) {
    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        if (args === null) {
            telegramClient.replyTextTo(message, "Ну а где варианты? Пришли варианты, разделенные слэшом (/)")
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

        telegramClient.replyTextTo(message, answerText)
    }
}
