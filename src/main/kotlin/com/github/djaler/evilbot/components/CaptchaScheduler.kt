package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.service.CaptchaService
import com.github.djaler.evilbot.utils.toUserId
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.members.kickChatMember
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.utils.formatting.link
import dev.inmo.tgbotapi.types.ParseMode.HTML
import dev.inmo.tgbotapi.types.link
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CaptchaScheduler(
    private val requestsExecutor: RequestsExecutor,
    private val captchaService: CaptchaService
) {
    private val parseMode = HTML

    @Scheduled(fixedRate = 1000 * 30)
    fun kickOverdue() {
        val overdueRestrictions = captchaService.getOverdueRestrictions()

        overdueRestrictions.forEach {
            GlobalScope.launch {
                val chatId = it.chat.telegramId.toChatId()
                val messageId = it.captchaMessageId
                val userId = it.memberTelegramId.toUserId()
                val cubeMessageId = it.cubeMessageId

                requestsExecutor.editMessageText(
                    chatId,
                    messageId,
                    "${("Ты" to userId.link).link(parseMode)} молчал слишком долго, прощай",
                    parseMode
                )

                requestsExecutor.kickChatMember(chatId, userId)

                captchaService.removeRestriction(chatId, userId)
                if (cubeMessageId != null) {
                    requestsExecutor.deleteMessage(chatId, cubeMessageId)
                }
            }
        }
    }
}
