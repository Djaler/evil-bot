package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.getForm
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UpdateStatisticHandler(
    private val chatService: ChatService,
    private val userService: UserService
) : MessageHandler() {
    override val order = 0

    @Transactional
    override fun handleMessage(message: Message): Boolean {
        if (message !is CommonMessageImpl<*>) {
            return false
        }
        val chat = message.chat as? PublicChat ?: return false

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
        val (userEntity, _) = userService.getOrCreateUserFrom(message.user)

        userService.registerMessageInStatistic(userEntity, chatEntity)

        return false
    }
}

@Component
class DisplayStatisticHandler(
    botInfo: User,
    private val chatService: ChatService,
    private val userService: UserService,
    private val telegramClient: TelegramClient
) : CommandHandler(
    botInfo,
    command = arrayOf("statistic")
) {
    override fun handleCommand(message: CommonMessageImpl<*>, args: List<String>) {
        val chat = message.chat as? PublicChat ?: return

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
        val (userEntity, _) = userService.getOrCreateUserFrom(message.user)

        val statistic = userService.getStatistic(userEntity, chatEntity)
        if (statistic == null) {
            telegramClient.replyTextTo(message, "Ты не писал ещё ничего, алло")
            return
        }

        val count = statistic.messagesCount

        telegramClient.replyTextTo(
            message,
            "Ты написал $count никому не ${count.getForm(
                "нужное сообщение",
                "нужных сообщения",
                "нужных сообщений"
            )}"
        )
    }
}


@Component
class DisplayTop10Handler(
    botInfo: User,
    private val chatService: ChatService,
    private val userService: UserService,
    private val telegramClient: TelegramClient
) : CommandHandler(
    botInfo,
    command = arrayOf("top10")
) {
    override fun handleCommand(message: CommonMessageImpl<*>, args: List<String>) {
        val chat = message.chat as? PublicChat ?: return

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val top = userService.getTop(chatEntity, limit = 10)

        if (top.isEmpty()) {
            return
        }

        val text = top
            .mapIndexed { index, statistic -> "${(index + 1)}. ${statistic.user.username} - ${statistic.messagesCount}" }
            .joinToString("\n")

        telegramClient.replyTextTo(message, text, disableNotification = true)
    }
}
