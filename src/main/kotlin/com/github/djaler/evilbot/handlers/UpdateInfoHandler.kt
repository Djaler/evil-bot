package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import io.sentry.SentryClient
import io.sentry.event.UserBuilder
import org.springframework.stereotype.Component

@Component
class UpdateInfoHandler(
    private val userService: UserService,
    private val chatService: ChatService,
    private val sentryClient: SentryClient
) : MessageHandler() {
    override val order = 0

    override suspend fun handleMessage(message: Message): Boolean {
        val userBuilder = UserBuilder()

        if (message is FromUserMessage) {
            userBuilder
                .setId(message.user.id.toString())
                .setUsername(message.user.usernameOrName)

            userService.getUser(message.user.id)?.let {
                val actualUsername = message.user.usernameOrName

                if (it.username != actualUsername) {
                    userService.updateUsername(it, actualUsername)
                }
            }
        }

        val chat = message.chat
        if (chat is PublicChat) {
            userBuilder
                .withData("chatTitle", chat.title)
                .withData("chatId", chat.id)

            chatService.getChat(chat.id)?.let {
                val actualTitle = chat.title

                if (it.title != actualTitle) {
                    chatService.updateTitle(it, actualTitle)
                }
            }
        }

        sentryClient.context.user = userBuilder.build()

        return false
    }
}
