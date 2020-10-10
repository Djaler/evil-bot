package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.entity.CaptchaRestriction
import com.github.djaler.evilbot.repository.CaptchaRestrictionRepository
import com.github.djaler.evilbot.utils.userId
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.User
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.abstracts.PossiblyReplyMessage
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
