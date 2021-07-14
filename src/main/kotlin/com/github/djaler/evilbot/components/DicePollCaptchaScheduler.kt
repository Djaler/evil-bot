package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.service.DicePollCaptchaService
import com.github.djaler.evilbot.utils.toUserId
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.members.unbanChatMember
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DicePollCaptchaScheduler(
    private val requestsExecutor: RequestsExecutor,
    private val captchaService: DicePollCaptchaService,
    private val exceptionsManager: ExceptionsManager,
    private val sentryClient: SentryClient
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    @Scheduled(fixedRate = 1000 * 30)
    fun kickOverdue() {
        val overdueRestrictions = captchaService.getOverdueRestrictions()

        overdueRestrictions.forEach {
            GlobalScope.launch {
                val chatId = it.chat.telegramId.toChatId()
                val userId = it.memberTelegramId.toUserId()

                try {
                    requestsExecutor.sendMessage(
                        chatId,
                        replyToMessageId = it.joinMessageId,
                        text = "Ты выбирал слишком долго, прощай"
                    )
                } catch (e: Exception) {
                    log.error("Restriction: $it", e)

                    exceptionsManager.process(e)

                    sentryClient.setExtra("restriction", it.toString())
                    sentryClient.captureException(e)
                }

                try {
                    /**
                     * Это используется вместо метода kick, так как он на самом деле не просто исключает из чата,
                     * а банит (на какой-то срок или навсегда).
                     * А у метода unban есть интересная особенность. Если человек сейчас в чате, то его просто выкинет,
                     * что нам и нужно. (https://core.telegram.org/bots/api#unbanchatmember)
                     */
                    requestsExecutor.unbanChatMember(chatId, userId)

                    requestsExecutor.deleteMessage(chatId, it.diceMessageId)
                    requestsExecutor.deleteMessage(chatId, it.pollMessageId)
                } catch (e: Exception) {
                    log.error("Restriction: $it", e)

                    exceptionsManager.process(e)

                    sentryClient.setExtra("restriction", it.toString())
                    sentryClient.captureException(e)
                } finally {
                    captchaService.removeRestriction(it)
                }
            }
        }
    }
}
