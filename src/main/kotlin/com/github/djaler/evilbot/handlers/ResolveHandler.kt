package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import kotlin.random.Random

@Component
class ResolveHandler(
    private val telegramClient: TelegramClient,
    botInfo: User
) : CommandHandler(botInfo.userName, command = arrayOf("r", "resolve")) {
    override fun handleCommand(message: Message, args: List<String>) {
        val variants = args.joinToString(" ")
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
