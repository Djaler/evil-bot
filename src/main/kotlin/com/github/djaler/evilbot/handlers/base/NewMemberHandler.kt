package com.github.djaler.evilbot.handlers.base

import com.github.djaler.evilbot.filters.message.MessageFilter
import dev.inmo.tgbotapi.extensions.utils.asChatEventMessage
import dev.inmo.tgbotapi.types.chat.Bot
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.message.ChatEvents.NewChatMembers
import dev.inmo.tgbotapi.types.message.abstracts.Message

abstract class NewMemberHandler(
    private val allowBots: Boolean = true,
    filter: MessageFilter? = null
) : MessageHandler(filter) {
    override suspend fun handleMessage(message: Message): Boolean {
        val newMembersEvent = message.asChatEventMessage()?.chatEvent as? NewChatMembers ?: return false

        val newMembers = newMembersEvent.members

        var anyHandled = false

        for (newMember in newMembers) {
            if (newMember is Bot && !allowBots) {
                continue
            }

            val handled = handleNewMember(newMember, message)
            if (handled) {
                anyHandled = true
            }
        }

        return anyHandled
    }

    abstract suspend fun handleNewMember(newMember: User, message: Message): Boolean
}
