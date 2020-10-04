package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.CasClient
import com.github.djaler.evilbot.utils.userId
import com.github.djaler.evilbot.utils.usernameOrName
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.members.kickChatMember
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.formatting.link
import com.github.insanusmokrassar.TelegramBotAPI.types.Bot
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.HTML
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.ChatEvents.NewChatMembers
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ChatEventMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class CasCheckHandler(
    private val casClient: CasClient,
    private val requestsExecutor: RequestsExecutor
) : MessageHandler() {
    override val order = 0

    private val parseMode = HTML

    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is ChatEventMessage) {
            return false
        }
        val chat = message.chat as? PublicChat ?: return false
        val newMembersEvent = message.chatEvent as? NewChatMembers ?: return false

        var anyBlocked = false

        for (member in newMembersEvent.members) {
            if (member is Bot) {
                continue
            }

            val casInfo = casClient.getCasInfo(member.id.userId)

            if (casInfo.result !== null) {
                requestsExecutor.sendMessage(
                    chat.id,
                    "${member.usernameOrName}, пошёл нахер! " +
                            "Ты забанен в ${("CAS" to "https://cas.chat/query?u=${member.id.userId}").link(parseMode)}",
                    parseMode = parseMode
                )
                requestsExecutor.kickChatMember(chat.id, member.id)

                anyBlocked = true
            }
        }

        return anyBlocked
    }
}
