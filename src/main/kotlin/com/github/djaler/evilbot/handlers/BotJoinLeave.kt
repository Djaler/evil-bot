package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.service.ChatService
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import dev.inmo.tgbotapi.types.message.ChatEvents.LeftChatMember
import dev.inmo.tgbotapi.types.message.ChatEvents.NewChatMembers
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class BotJoinHandler(
    private val botInfo: ExtendedBot,
    private val chatService: ChatService
) : MessageHandler() {
    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is ChatEventMessage<*>) {
            return false
        }
        val chat = message.chat as? PublicChat ?: return false
        val newMembersEvent = message.chatEvent as? NewChatMembers ?: return false

        newMembersEvent.members
            .find { it.id == botInfo.id }
            ?.let {
                val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
                chatService.fixChatJoin(chatEntity)
            }

        return false
    }
}

@Component
class BotLeaveHandler(
    private val botInfo: ExtendedBot,
    private val chatService: ChatService
) : MessageHandler() {
    override val order = 0

    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is ChatEventMessage<*>) {
            return false
        }
        val chat = message.chat as? PublicChat ?: return false
        val leftMemberEvent = message.chatEvent as? LeftChatMember ?: return false

        if (leftMemberEvent.user.id == botInfo.id) {
            val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
            chatService.fixChatLeave(chatEntity)

            return true
        }

        return false
    }
}

