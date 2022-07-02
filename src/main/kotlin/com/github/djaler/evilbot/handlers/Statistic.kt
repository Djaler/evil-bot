package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.handlers.base.CommonMessageHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.getForm
import com.github.djaler.evilbot.utils.getFormByGender
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asFromUserMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UpdateStatisticHandler(
    private val chatService: ChatService,
    private val userService: UserService
) : CommonMessageHandler() {
    override val order = 0

    @Transactional
    override suspend fun handleMessage(message: CommonMessage<*>): Boolean {
        if (message !is FromUserMessage) {
            return false
        }

        val chat = message.chat.asPublicChat() ?: return false

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
        val (userEntity, _) = userService.getOrCreateUserFrom(message.user)

        userService.registerMessageInStatistic(userEntity, chatEntity)

        return false
    }
}

@Component
class DisplayStatisticHandler(
    botInfo: ExtendedBot,
    private val chatService: ChatService,
    private val userService: UserService,
    private val requestsExecutor: RequestsExecutor
) : CommandHandler(
    botInfo,
    command = arrayOf("statistic"),
    commandDescription = "сколько сообщений ты написал"
) {
    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        val chat = message.chat.asPublicChat() ?: return
        val user = message.asFromUserMessage()?.user ?: return

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
        val (userEntity, _) = userService.getOrCreateUserFrom(user)

        val statistic = userService.getStatistic(userEntity, chatEntity)
        if (statistic == null) {
            requestsExecutor.reply(
                message,
                "Ты не ${userEntity.gender.getFormByGender("писал", "писала", "писало")} ещё ничего, алло",
            )
            return
        }

        val count = statistic.messagesCount

        requestsExecutor.reply(
            message,
            "Ты ${userEntity.gender.getFormByGender("написал", "написала", "написало")} $count никому не ${
                count.getForm(
                    "нужное сообщение",
                    "нужных сообщения",
                    "нужных сообщений"
                )
            }"
        )
    }
}


@Component
class DisplayTop10Handler(
    botInfo: ExtendedBot,
    private val chatService: ChatService,
    private val userService: UserService,
    private val requestsExecutor: RequestsExecutor
) : CommandHandler(
    botInfo,
    command = arrayOf("top10"),
    commandDescription = "кто больше всех пишет"
) {
    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        val chat = message.chat.asPublicChat() ?: return

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val top = userService.getTop(chatEntity, limit = 10)

        if (top.isEmpty()) {
            return
        }

        val text = top
            .mapIndexed { index, statistic -> "${(index + 1)}. ${statistic.user.username} - ${statistic.messagesCount}" }
            .joinToString("\n")

        requestsExecutor.reply(message, text, disableNotification = true)
    }
}
