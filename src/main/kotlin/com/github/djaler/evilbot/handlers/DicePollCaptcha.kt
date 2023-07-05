package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramMediaSender
import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.entity.DicePollCaptchaRestriction
import com.github.djaler.evilbot.filters.message.CanRestrictMemberMessageFilter
import com.github.djaler.evilbot.handlers.base.NewMemberHandler
import com.github.djaler.evilbot.handlers.base.PollAnswerHandler
import com.github.djaler.evilbot.service.DicePollCaptchaService
import com.github.djaler.evilbot.utils.getForm
import com.github.djaler.evilbot.utils.toUserId
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.restrictChatMember
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.forwardMessage
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendDice
import dev.inmo.tgbotapi.extensions.utils.asGroupChat
import dev.inmo.tgbotapi.extensions.utils.asRestrictedChatMember
import dev.inmo.tgbotapi.types.chat.*
import dev.inmo.tgbotapi.types.dartsCubeAndBowlingDiceResultLimit
import dev.inmo.tgbotapi.types.dice.CubeDiceAnimationType
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.polls.PollAnswer
import dev.inmo.tgbotapi.types.toChatId
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class DicePollCaptchaSendHandler(
    private val requestsExecutor: RequestsExecutor,
    private val captchaService: DicePollCaptchaService,
    private val botProperties: BotProperties,
    canRestrictMemberFilter: CanRestrictMemberMessageFilter
) : NewMemberHandler(allowBots = false, filter = canRestrictMemberFilter) {
    override suspend fun handleNewMember(newMember: User, message: Message): Boolean {
        val chat = message.chat.asGroupChat() ?: return false

        val previousRestriction = captchaService.getRestriction(chat.id, newMember.id)
        if (previousRestriction != null) {
            forwardPreviousCaptcha(chat, previousRestriction)
        } else {
            createCaptcha(chat, newMember, message)
        }

        return true
    }

    private suspend fun forwardPreviousCaptcha(chat: GroupChat, previousRestriction: DicePollCaptchaRestriction) {
        val newDiceMessage = requestsExecutor.forwardMessage(chat, chat, previousRestriction.diceMessageId)
        requestsExecutor.deleteMessage(chat, previousRestriction.diceMessageId)
        val newPollMessage = requestsExecutor.forwardMessage(chat, chat, previousRestriction.pollMessageId)
        requestsExecutor.deleteMessage(chat, previousRestriction.pollMessageId)
        captchaService.updateRestriction(previousRestriction, newDiceMessage, newPollMessage)
    }

    private suspend fun createCaptcha(chat: GroupChat, member: User, message: Message) {
        val chatMember = requestsExecutor.getChatMember(chat.id, member.id)

        val originalPermissions =
            chatMember.asRestrictedChatMember() ?: LeftRestrictionsChatPermissions

        requestsExecutor.restrictChatMember(chat.id, member.id, permissions = RestrictionsChatPermissions)

        val diceMessage = requestsExecutor.sendDice(chat.id, CubeDiceAnimationType)
        val cubeValue = diceMessage.content.dice.value

        val kickTimeoutMinutes = botProperties.captchaKickTimeout.toMinutes()

        val options = dartsCubeAndBowlingDiceResultLimit.shuffled()
        val correctIndex = options.indexOf(cubeValue)

        val pollMessage = requestsExecutor.reply(
            diceMessage,
            """
                Эй, ${member.usernameOrName}! Мы отобрали твою свободу слова, пока ты не тыкнешь число, выпавшее сверху на кубике 👇
                У тебя есть $kickTimeoutMinutes ${kickTimeoutMinutes.getForm("минута", "минуты", "минут")}
                """.trimIndent(),
            options = options.map { it.toString() },
            isAnonymous = false,
            allowMultipleAnswers = true,
        )

        captchaService.fixRestriction(
            chat,
            member,
            message,
            diceMessage,
            pollMessage,
            correctIndex,
            originalPermissions
        )
    }
}

@Component
class DicePollCaptchaAnswerHandler(
    private val requestsExecutor: RequestsExecutor,
    private val telegramMediaSender: TelegramMediaSender,
    private val captchaService: DicePollCaptchaService
) : PollAnswerHandler() {
    companion object {
        private val welcomeGif = ClassPathResource("media/welcome_to_the_club.mp4")
    }

    override suspend fun handleAnswer(answer: PollAnswer) {
        val restriction = captchaService.getRestrictionForPollOrNull(answer.pollId) ?: return

        val chatId = restriction.chat.telegramId.toChatId()
        val userId = restriction.memberTelegramId.toUserId()

        if (answer.user.id != userId) {
            return
        }

        val correct = answer.chosen.size == 1 && answer.chosen.first() == restriction.correctAnswerIndex

        if (!correct) {
            return
        }

        requestsExecutor.restrictChatMember(
            chatId,
            userId,
            permissions = restriction.chatPermissions
        )
        captchaService.removeRestriction(restriction)

        requestsExecutor.deleteMessage(chatId, restriction.diceMessageId)
        requestsExecutor.deleteMessage(chatId, restriction.pollMessageId)

        telegramMediaSender.sendAnimation(chatId, welcomeGif, replyTo = restriction.joinMessageId)
    }
}

private val DicePollCaptchaRestriction.chatPermissions: ChatPermissions
    get() = ChatPermissions(
        canSendMessages = canSendMessages,
        canSendAudios = canSendAudios,
        canSendDocuments = canSendDocuments,
        canSendPhotos = canSendPhotos,
        canSendVideos = canSendVideos,
        canSendVideoNotes = canSendVideoNotes,
        canSendVoiceNotes = canSendVoiceNotes,
        canSendPolls = canSendPolls,
        canSendOtherMessages = canSendOtherMessages,
        canAddWebPagePreviews = canAddWebPagePreviews,
        canChangeInfo = canChangeInfo,
        canInviteUsers = canInviteUsers,
        canPinMessages = canPinMessages
    )
