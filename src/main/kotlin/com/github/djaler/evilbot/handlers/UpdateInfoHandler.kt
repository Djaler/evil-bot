package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.not
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.usernameOrName
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message

@Component
class UpdateInfoHandler(
    private val userService: UserService,
    private val chatService: ChatService
) : MessageHandler(filter = Filters.PrivateChat.not()) {
    override val order = 0

    override fun handleMessage(message: Message): Boolean {
        val user = userService.getUser(message.from.id)

        if (user != null) {
            val actualUsername = message.from.usernameOrName

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
