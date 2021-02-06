package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.entity.DicePollCaptchaRestriction
import com.github.djaler.evilbot.filters.message.CanRestrictMemberMessageFilter
import com.github.djaler.evilbot.service.DicePollCaptchaService
import com.github.djaler.evilbot.utils.*
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.chat.members.restrictChatMember
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.forwardMessage
import dev.inmo.tgbotapi.extensions.api.send.media.sendAnimation
import dev.inmo.tgbotapi.extensions.api.send.polls.replyWithRegularPoll
import dev.inmo.tgbotapi.extensions.api.send.sendDice
import dev.inmo.tgbotapi.extensions.utils.asGroupMessage
import dev.inmo.tgbotapi.extensions.utils.asRestrictedChatMember
import dev.inmo.tgbotapi.requests.abstracts.MultipartFile
import dev.inmo.tgbotapi.types.Bot
import dev.inmo.tgbotapi.types.User
import dev.inmo.tgbotapi.types.chat.ChatPermissions
import dev.inmo.tgbotapi.types.chat.abstracts.GroupChat
import dev.inmo.tgbotapi.types.dartsCubeAndBowlingDiceResultLimit
import dev.inmo.tgbotapi.types.dice.CubeDiceAnimationType
import dev.inmo.tgbotapi.types.message.ChatEvents.NewChatMembers
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
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
) : MessageHandler(filter = canRestrictMemberFilter) {

    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is ChatEventMessage<*>) {
            return false
        }
        val chat = message.asGroupMessage()?.chat ?: return false
        val newMembersEvent = message.chatEvent as? NewChatMembers ?: return false

        var anyUser = false

        val newMembers = newMembersEvent.members.filter { it !is Bot }

        if (newMembers.isEmpty()) {
            return false
        }

        newMembers.forEach { member ->
            val previousRestriction = captchaService.getRestriction(chat.id, member.id)
            if (previousRestriction != null) {
                forwardPreviousCaptcha(chat, previousRestriction)
            } else {
                createCaptcha(chat, member, message)
            }
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

    private suspend fun createCaptcha(chat: GroupChat, member: User, message: ChatEventMessage<*>) {
        val chatMember = requestsExecutor.getChatMember(chat.id, member.id)

        val originalPermissions = chatMember.asRestrictedChatMember()?.chatPermissions ?: fullChatPermissions

        requestsExecutor.restrictChatMember(chat.id, member.id)

        val diceMessage = requestsExecutor.sendDice(chat.id, CubeDiceAnimationType)
        val cubeValue = diceMessage.content.dice.value

        val kickTimeoutMinutes = botProperties.captchaKickTimeout.toMinutes()

        val options = dartsCubeAndBowlingDiceResultLimit.shuffled()
        val correctIndex = options.indexOf(cubeValue)

        val pollMessage = requestsExecutor.replyWithRegularPoll(
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
    private val captchaService: DicePollCaptchaService
) : PollAnswerHandler() {
    companion object {
        private val welcomeGif by lazy {
            MultipartFile(StorageFile(ClassPathResource("media/welcome_to_the_club.mp4")))
        }
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

        requestsExecutor.sendAnimation(
            chatId,
            welcomeGif,
            replyToMessageId = restriction.joinMessageId
        )
    }
}

private val DicePollCaptchaRestriction.chatPermissions: ChatPermissions
    get() = ChatPermissions(
        canSendMessages = canSendMessages,
        canSendMediaMessages = canSendMediaMessages,
        canSendPolls = canSendPolls,
        canSendOtherMessages = canSendOtherMessages,
        canAddWebPagePreviews = canAddWebPagePreviews,
        canChangeInfo = canChangeInfo,
        canInviteUsers = canInviteUsers,
        canPinMessages = canPinMessages
    )