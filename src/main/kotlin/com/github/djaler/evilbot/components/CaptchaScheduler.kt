package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.service.CaptchaService
import com.github.djaler.evilbot.utils.toUserId
import com.github.insanusmokrassar.TelegramBotAPI.types.link
import com.github.insanusmokrassar.TelegramBotAPI.types.toChatId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CaptchaScheduler(
    private val telegramClient: TelegramClient,
    private val captchaService: CaptchaService
) {
    @Scheduled(fixedRate = 1000 * 30)
    fun kickOverdue() {
        val overdueRestrictions = captchaService.getOverdueRestrictions()

        overdueRestrictions.forEach {
            GlobalScope.launch {
                val chatId = it.chat.telegramId.toChatId()
                val userId = it.memberTelegramId.toUserId()
                telegramClient.sendTextTo(
                    chatId,
                    "[Ты](${userId.link}) молчал слишком долго, прощай",
                    enableMarkdown = true
                )

                telegramClient.kickChatMember(chatId, userId)

                captchaService.removeRestriction(chatId, userId)
            }
        }
    }
}
