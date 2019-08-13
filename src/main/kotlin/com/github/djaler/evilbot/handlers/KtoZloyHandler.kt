package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.not
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import kotlin.random.Random

@Component
class KtoZloyHandler(
    botUsername: String,
    private val chatService: ChatService,
    private val userService: UserService,
    private val telegramClient: TelegramClient
) : CommandHandler(
    botUsername,
    command = arrayOf("ktozloy"),
    filter = Filters.PrivateChat.not()
) {
    override fun handleCommand(message: Message, args: List<String>) {
        if (Random.nextInt(0, 10) == 0) {
            telegramClient.replyTextTo(message, "я злой ¯\\_(ツ)_/¯")
            return
        }

        val (chat, _) = chatService.getOrCreateChatFrom(message.chat)

        val randomUser = userService.getLatest(chat, 10).random().user

        val username = if (randomUser.telegramId == message.from.id) "ты" else randomUser.username

        telegramClient.replyTextTo(message, "$username злой")
    }
}
