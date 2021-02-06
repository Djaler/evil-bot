package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import io.sentry.protocol.User
import org.springframework.stereotype.Component

@Component
class UpdateInfoHandler(
    private val userService: UserService,
    private val chatService: ChatService,
    private val sentryClient: SentryClient
) : MessageHandler() {
    override val order = 0

    override suspend fun handleMessage(message: Message): Boolean {
        val sentryUser = User()

        if (message is FromUserMessage) {
            sentryUser.id = message.user.id.toString()
            sentryUser.username = message.user.usernameOrName

            userService.getUser(message.user.id)?.let {
                val actualUsername = message.user.usernameOrName

                if (it.username != actualUsername) {
                    userService.updateUsername(it, actualUsername)
                }
            }
        }

        val chat = message.chat
        if (chat is PublicChat) {
            sentryUser.others = mapOf(
                "chatTitle" to chat.title,
                "chatId" to chat.id.toString()
            )

            chatService.getChat(chat.id)?.let {
                val actualTitle = chat.title

                if (it.title != actualTitle) {
                    chatService.updateTitle(it, actualTitle)
                }
            }
        }

        sentryClient.setUser(sentryUser)

        return false
    }
}
