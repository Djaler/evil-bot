package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.service.CaptchaService
import com.github.djaler.evilbot.utils.toUserId
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
    private val telegramClient: TelegramClient,
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

                telegramClient.changeText(
                    chatId,
                    messageId,
                    "${("Ты" to userId.link).link(parseMode)} молчал слишком долго, прощай",
                    parseMode
                )

                telegramClient.kickChatMember(chatId, userId)

                captchaService.removeRestriction(chatId, userId)
                if (cubeMessageId != null) {
                    telegramClient.deleteMessage(chatId, cubeMessageId)
                }
            }
        }
    }
}
