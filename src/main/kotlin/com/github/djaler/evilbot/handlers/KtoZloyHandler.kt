package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.getFormByGender
import com.github.djaler.evilbot.utils.userId
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicMessage
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
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
    override suspend fun <M> handleCommand(
        message: M,
        args: String?
    ) where M : CommonMessage<TextContent>, M : FromUserMessage {
        val chat = message.asPublicMessage()?.chat ?: return

        if (Random.nextInt(0, 10) == 0) {
            requestsExecutor.sendMessage(message.chat, "я злой ¯\\_(ツ)_/¯")
            return
        }

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val randomUser = userService.getLatest(chatEntity, 10).random().user

        val username = if (randomUser.telegramId == message.user.id.userId) "ты" else randomUser.username

        requestsExecutor.reply(message, "$username ${randomUser.gender.getFormByGender("злой", "злая", "злое")}")
    }
}
