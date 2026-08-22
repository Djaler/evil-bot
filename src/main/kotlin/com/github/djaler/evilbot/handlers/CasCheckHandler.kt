package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.CasClient
import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.handlers.base.NewMemberHandler
import com.github.djaler.evilbot.utils.userId
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.members.banChatMember
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.message.abstracts.AccessibleMessage
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.link
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component

@Component
class CasCheckHandler(
    private val casClient: CasClient,
    private val sentryClient: SentryClient,
    private val requestsExecutor: RequestsExecutor
) : NewMemberHandler(allowBots = false) {
    companion object {
        private val log = LogManager.getLogger()
    }

    override val order = 0

    override suspend fun handleNewMember(newMember: User, message: AccessibleMessage): Boolean {
        val casInfo = try {
            casClient.getCasInfo(newMember.id.userId)
        } catch (e: Throwable) {
            log.error("CAS check failed for user ${newMember.id.userId}, fail-open", e)
            sentryClient.captureException(e)
            return false
        }
        if (casInfo.result === null) {
            return false
        }

        requestsExecutor.sendMessage(
            message.chat.id,
            buildEntities(separator = " ") {
                +"${newMember.usernameOrName}, пошёл нахер!"
                +"Ты забанен в" + link("CAS", "https://cas.chat/query?u=${newMember.id.userId}")
            }
        )
        requestsExecutor.banChatMember(message.chat.id, newMember.id)
        return true
    }
}
