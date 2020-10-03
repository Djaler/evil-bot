package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.getFormByGender
import com.github.djaler.evilbot.utils.userId
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class KtoZloyHandler(
    botInfo: ExtendedBot,
    private val chatService: ChatService,
    private val userService: UserService,
    private val requestsExecutor: RequestsExecutor
) : CommandHandler(
    botInfo,
    command = arrayOf("ktozloy"),
    commandDescription = "выясняет, кто злой"
) {
    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val chat = message.chat as? PublicChat ?: return

        if (Random.nextInt(0, 10) == 0) {
            requestsExecutor.sendMessage(message.chat, "я злой ¯\\_(ツ)_/¯")
            return
        }

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val randomUser = userService.getLatest(chatEntity, 10).random().user

        val username = if (randomUser.telegramId == message.user.id.userId) "ты" else randomUser.username

        requestsExecutor.sendMessage(message.chat, "$username ${randomUser.getFormByGender("злой", "злая")}", replyToMessageId = message.messageId)
    }
}
