package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.CasClient
import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.utils.userId
import com.github.djaler.evilbot.utils.usernameOrName
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.ChatEvents.NewChatMembers
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ChatEventMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class CasCheckHandler(
    private val casClient: CasClient,
    private val telegramClient: TelegramClient
) : MessageHandler() {
    override val order = 0

    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is ChatEventMessage) {
            return false
        }
        val chat = message.chat as? PublicChat ?: return false
        val newMembersEvent = message.chatEvent as? NewChatMembers ?: return false

        var anyBlocked = false

        for (member in newMembersEvent.members) {
            if (member.isBot) {
                continue
            }

            val casInfo = casClient.getCasInfo(member.id.userId)

            if (casInfo.result !== null) {
                telegramClient.kickChatMember(chat.id, member.id)
                telegramClient.sendTextTo(
                    chat.id,
                    "${member.usernameOrName}, пошёл нахер! Ты забанен в CAS за сообщение \"${casInfo.result.messages.random()}\""
                )

                anyBlocked = true
            }
        }

        return anyBlocked
    }
}
