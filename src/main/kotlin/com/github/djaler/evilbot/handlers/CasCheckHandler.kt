package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.CasClient
import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.and
import com.github.djaler.evilbot.filters.not
import com.github.djaler.evilbot.utils.usernameOrName
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message

@Component
class CasCheckHandler(
    private val casClient: CasClient,
    private val telegramClient: TelegramClient
) : MessageHandler(filter = Filters.PrivateChat.not() and Filters.NewChatMember) {
    override val order = 0

    override fun handleMessage(message: Message): Boolean {
        val newMembers = message.newChatMembers

        var anyBlocked = false

        for (member in newMembers) {
            if (member.bot) {
                continue
            }

            val casInfo = casClient.getCasInfo(member.id)

            if (casInfo.result !== null) {
                telegramClient.kickChatMember(message.chatId, member.id)
                telegramClient.sendTextTo(
                    message.chatId,
                    "${member.usernameOrName}, пошёл нахер! Ты забанен в CAS за сообщение \"${casInfo.result.messages.random()}\""
                )

                anyBlocked = true
            }
        }

        return anyBlocked
    }
}
