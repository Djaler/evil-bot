package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.base.CommonMessageHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
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
