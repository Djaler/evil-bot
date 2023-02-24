package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.base.MessageHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import dev.inmo.tgbotapi.extensions.utils.asChatEventMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.types.message.ChatEvents.MigratedToSupergroup
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class UpdateStatisticHandler(
    private val chatService: ChatService,
    private val userService: UserService
) : MessageHandler() {

    override val order = 0

    override suspend fun handleMessage(message: Message): Boolean {
        val chat = message.chat.asPublicChat() ?: return false

        val migratedToSupergroup = message.asChatEventMessage()?.chatEvent as? MigratedToSupergroup
        if (migratedToSupergroup != null) {
            chatService.updateChatId(chat.id, migratedToSupergroup.migratedFrom)
        }

        if (message is FromUserMessage) {
            val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
            val (userEntity, _) = userService.getOrCreateUserFrom(message.user)

            userService.registerMessageInStatistic(userEntity, chatEntity)
        }

        return false
    }
}
