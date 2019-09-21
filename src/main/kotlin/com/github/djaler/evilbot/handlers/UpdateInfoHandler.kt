package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.not
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.usernameOrName
import io.sentry.SentryClient
import io.sentry.event.UserBuilder
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message

@Component
class UpdateInfoHandler(
    private val userService: UserService,
    private val chatService: ChatService,
    private val sentryClient: SentryClient
) : MessageHandler(filter = Filters.PrivateChat.not()) {
    override val order = 0

    override fun handleMessage(message: Message): Boolean {
        val fromUser = message.from

        sentryClient.context.user = UserBuilder()
            .setId(fromUser.id.toString())
            .setUsername(fromUser.usernameOrName)
            .withData("chatTitle", message.chat.title)
            .withData("chatId", message.chat.id)
            .build()

        val user = userService.getUser(fromUser.id)

        if (user != null) {
            val actualUsername = fromUser.usernameOrName

            if (user.username != actualUsername) {
                userService.updateUsername(user, actualUsername)
            }
        }

        val chat = chatService.getChat(message.chatId)

        if (chat != null) {
            val actualTitle = message.chat.title

            if (chat.title != actualTitle) {
                chatService.updateTitle(chat, actualTitle)
            }
        }

        return false
    }
}
