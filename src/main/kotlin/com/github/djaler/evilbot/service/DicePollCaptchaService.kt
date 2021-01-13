package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.entity.DicePollCaptchaRestriction
import com.github.djaler.evilbot.repository.DicePollCaptchaRestrictionRepository
import com.github.djaler.evilbot.utils.userId
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.PollIdentifier
import dev.inmo.tgbotapi.types.User
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.ChatPermissions
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.DiceContent
import dev.inmo.tgbotapi.types.message.content.PollContent
import org.hibernate.exception.ConstraintViolationException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DicePollCaptchaService(
    private val chatService: ChatService,
    private val captchaRestrictionRepository: DicePollCaptchaRestrictionRepository,
    private val botProperties: BotProperties
) {
    fun fixRestriction(
        chat: PublicChat,
        member: User,
        joinMessage: ChatEventMessage,
        diceMessage: ContentMessage<DiceContent>,
        pollMessage: ContentMessage<PollContent>,
        correctAnswerIndex: Int,
        permissions: ChatPermissions
    ) {
        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        try {
            captchaRestrictionRepository.save(
                DicePollCaptchaRestriction(
                    chatEntity,
                    member.id.userId,
                    LocalDateTime.now(),
                    joinMessage.messageId,
                    diceMessage.messageId,
                    pollMessage.messageId,
                    pollMessage.content.poll.id,
                    correctAnswerIndex,
                    canSendMessages = permissions.canSendMessages,
                    canSendMediaMessages = permissions.canSendMediaMessages,
                    canSendPolls = permissions.canSendPolls,
                    canSendOtherMessages = permissions.canSendOtherMessages,
                    canAddWebPagePreviews = permissions.canAddWebPagePreviews,
                    canChangeInfo = permissions.canChangeInfo,
                    canInviteUsers = permissions.canInviteUsers,
                    canPinMessages = permissions.canPinMessages
                )
            )
        } catch (e: ConstraintViolationException) {
            // ignore duplicate
        }
    }

    fun removeRestriction(restriction: DicePollCaptchaRestriction) {
        captchaRestrictionRepository.delete(restriction)
    }

    fun getOverdueRestrictions(): List<DicePollCaptchaRestriction> {
        val overdueDate = LocalDateTime.now().minus(botProperties.captchaKickTimeout)

        return captchaRestrictionRepository.findByDateTimeBefore(overdueDate)
    }

    fun getRestriction(chatId: ChatId, memberId: UserId): DicePollCaptchaRestriction? {
        return captchaRestrictionRepository.findByChatTelegramIdAndMemberTelegramId(chatId.chatId, memberId.userId)
    }

    fun getRestrictionForPollOrNull(pollId: PollIdentifier): DicePollCaptchaRestriction? {
        return captchaRestrictionRepository.findByPollId(pollId)
    }

    fun updateRestriction(restriction: DicePollCaptchaRestriction, diceMessage: Message, pollMessage: Message) {
        captchaRestrictionRepository.save(
            restriction.copy(
                diceMessageId = diceMessage.messageId,
                pollMessageId = pollMessage.messageId,
                dateTime = LocalDateTime.now()
            )
        )
    }
}
