package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.service.CaptchaService
import com.github.djaler.evilbot.utils.toUserId
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.members.kickChatMember
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.deleteMessage
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.edit.text.editMessageText
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.HTML
import com.github.insanusmokrassar.TelegramBotAPI.types.link
import com.github.insanusmokrassar.TelegramBotAPI.types.toChatId
import com.github.insanusmokrassar.TelegramBotAPI.utils.link
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
