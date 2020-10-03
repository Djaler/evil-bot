package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.entity.CaptchaRestriction
import com.github.djaler.evilbot.repository.CaptchaRestrictionRepository
import com.github.djaler.evilbot.utils.userId
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.UserId
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.PossiblyReplyMessage
import org.hibernate.exception.ConstraintViolationException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CaptchaService(
    private val chatService: ChatService,
    private val captchaRestrictionRepository: CaptchaRestrictionRepository,
    private val botProperties: BotProperties
) {
    fun fixRestriction(
        chat: PublicChat,
        member: User,
        captchaMessage: Message
    ) {
        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
        val cubeMessageId = (captchaMessage as? PossiblyReplyMessage)?.replyTo?.messageId

        try {
            captchaRestrictionRepository.save(
                CaptchaRestriction(
                    chatEntity,
                    member.id.userId,
                    LocalDateTime.now(),
                    captchaMessage.messageId,
                    cubeMessageId
                )
            )
        } catch (e: ConstraintViolationException) {
            // ignore duplicate
        }
    }

    fun removeRestriction(chatId: ChatId, memberId: UserId) {
        val chatEntity = chatService.getChat(chatId) ?: return

        captchaRestrictionRepository.deleteByChatAndMember(chatEntity.id, memberId.userId)
    }

    fun getOverdueRestrictions(): List<CaptchaRestriction> {
        val overdueDate = LocalDateTime.now().minus(botProperties.captchaKickTimeout)

        return captchaRestrictionRepository.findByDateTimeBefore(overdueDate)
    }
}
