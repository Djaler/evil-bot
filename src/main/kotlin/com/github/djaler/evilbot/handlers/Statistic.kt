package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.components.TelegramLinksHelper
import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.not
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.getForm
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.Message

@Component
class UpdateStatisticHandler(
    private val chatService: ChatService,
    private val userService: UserService
) : MessageHandler(Filters.PrivateChat.not()) {
    override val order = 0

    @Transactional
    override fun handleMessage(message: Message): Boolean {
        val (chat, _) = chatService.getOrCreateChatFrom(message.chat)
        val (user, _) = userService.getOrCreateUserFrom(message.from)

        userService.registerMessageInStatistic(user, chat)

        return false
    }
}

@Component
class DisplayStatisticHandler(
    botUsername: String,
    private val chatService: ChatService,
    private val userService: UserService,
    private val telegramClient: TelegramClient
) : CommandHandler(
    botUsername,
    command = arrayOf("statistic"),
    filter = Filters.PrivateChat.not()
) {
    override fun handleCommand(message: Message, args: List<String>) {
        val (chat, _) = chatService.getOrCreateChatFrom(message.chat)
        val (user, _) = userService.getOrCreateUserFrom(message.from)

        val statistic = userService.getStatistic(user, chat)
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
    botUsername: String,
    private val chatService: ChatService,
    private val userService: UserService,
    private val telegramClient: TelegramClient,
    private val telegramLinksHelper: TelegramLinksHelper
) : CommandHandler(
    botUsername,
    command = arrayOf("top10"),
    filter = Filters.PrivateChat.not()
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    override fun handleCommand(message: Message, args: List<String>) {
        val (chat, _) = chatService.getOrCreateChatFrom(message.chat)

        val top = userService.getTop(chat, limit = 10)

        if (top.isEmpty()) {
            return
        }

        val text = top
            .mapIndexed { index, statistic -> "${(index + 1)}. ${statistic.user.username} - ${statistic.messagesCount}" }
            .joinToString("\n")

        telegramClient.replyTextTo(message, text, disableNotification = true)

        return
    }
}
