package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.userId
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class KtoZloyHandler(
    botInfo: User,
    private val chatService: ChatService,
    private val userService: UserService,
    private val telegramClient: TelegramClient
) : CommandHandler(
    botInfo,
    command = arrayOf("ktozloy")
) {
    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val chat = message.chat as? PublicChat ?: return

        if (Random.nextInt(0, 10) == 0) {
            telegramClient.replyTextTo(message, "я злой ¯\\_(ツ)_/¯")
            return
        }

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val randomUser = userService.getLatest(chatEntity, 10).random().user

        val username = if (randomUser.telegramId == message.user.id.userId) "ты" else randomUser.username

        telegramClient.replyTextTo(message, "$username злой")
    }
}
