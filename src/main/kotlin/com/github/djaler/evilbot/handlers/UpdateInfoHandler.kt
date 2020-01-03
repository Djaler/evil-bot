package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.usernameOrName
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import io.sentry.SentryClient
import io.sentry.event.UserBuilder
import org.springframework.stereotype.Component

@Component
class UpdateInfoHandler(
    private val userService: UserService,
    private val chatService: ChatService,
    private val sentryClient: SentryClient
) : CommonMessageHandler() {
    override val order = 0

    override suspend fun handleMessage(message: CommonMessageImpl<*>): Boolean {
        val chat = message.chat as? PublicChat ?: return false
        val fromUser = message.user

        sentryClient.context.user = UserBuilder()
            .setId(fromUser.id.toString())
            .setUsername(fromUser.usernameOrName)
            .withData("chatTitle", chat.title)
            .withData("chatId", chat.id)
            .build()

        userService.getUser(fromUser.id)?.let {
            val actualUsername = fromUser.usernameOrName

            if (it.username != actualUsername) {
                userService.updateUsername(it, actualUsername)
            }
        }

        chatService.getChat(chat.id)?.let {
            val actualTitle = chat.title

            if (it.title != actualTitle) {
                chatService.updateTitle(it, actualTitle)
            }
        }

        return false
    }
}
