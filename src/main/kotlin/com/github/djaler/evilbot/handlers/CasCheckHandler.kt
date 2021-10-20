package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.CasClient
import com.github.djaler.evilbot.utils.userId
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.members.banChatMember
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.asChatEventMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.formatting.buildEntities
import dev.inmo.tgbotapi.extensions.utils.formatting.link
import dev.inmo.tgbotapi.types.Bot
import dev.inmo.tgbotapi.types.message.ChatEvents.NewChatMembers
import dev.inmo.tgbotapi.types.message.abstracts.Message
import org.springframework.stereotype.Component

@Component
class CasCheckHandler(
    private val casClient: CasClient,
    private val requestsExecutor: RequestsExecutor
) : MessageHandler() {
    override val order = 0

    override suspend fun handleMessage(message: Message): Boolean {
        val chat = message.chat.asPublicChat() ?: return false
        val newMembersEvent = message.asChatEventMessage()?.chatEvent as? NewChatMembers ?: return false

        var anyBlocked = false

        for (member in newMembersEvent.members) {
            if (member is Bot) {
                continue
            }

            val casInfo = casClient.getCasInfo(member.id.userId)

            if (casInfo.result !== null) {
                requestsExecutor.sendMessage(
                    chat.id,
                    buildEntities(separator = "") {
                        +"${member.usernameOrName}, пошёл нахер!"
                        +"Ты забанен в" + link("CAS", "https://cas.chat/query?u=${member.id.userId}")
                    }
                )
                requestsExecutor.banChatMember(chat.id, member.id)

                anyBlocked = true
            }
        }

        return anyBlocked
    }
}
