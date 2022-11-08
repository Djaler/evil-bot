package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.getFormByGender
import com.github.djaler.evilbot.utils.userId
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.asFromUserMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
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
    commandDescription = "выясняет, кто злой",
    commandScope = BotCommandScope.AllGroupChats,
) {
    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        val chat = message.chat.asPublicChat() ?: return
        val user = message.asFromUserMessage()?.user

        if (Random.nextInt(0, 10) == 0) {
            requestsExecutor.sendMessage(message.chat, "я злой ¯\\_(ツ)_/¯")
            return
        }

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val randomUser = userService.getLatest(chatEntity, 5).random().user

        val username = if (randomUser.telegramId == user?.id?.userId) "ты" else randomUser.username

        requestsExecutor.reply(message, "$username ${randomUser.gender.getFormByGender("злой", "злая", "злое")}")
    }
}
