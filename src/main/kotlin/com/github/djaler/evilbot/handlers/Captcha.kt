package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.filters.message.CanRestrictMemberMessageFilter
import com.github.djaler.evilbot.service.CaptchaService
import com.github.djaler.evilbot.utils.*
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.FileId
import com.github.insanusmokrassar.TelegramBotAPI.types.*
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatMember.RestrictedChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.ChatPermissions
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.GroupChat
import com.github.insanusmokrassar.TelegramBotAPI.types.dice.CubeDiceAnimationType
import com.github.insanusmokrassar.TelegramBotAPI.types.message.ChatEvents.NewChatMembers
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ChatEventMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.PossiblyReplyMessage
import org.springframework.stereotype.Component

@Component
class SendCaptchaHandler(
    private val telegramClient: TelegramClient,
    private val captchaService: CaptchaService,
    private val botProperties: BotProperties,
    canRestrictMemberFilter: CanRestrictMemberMessageFilter
) : MessageHandler(filter = canRestrictMemberFilter) {

    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is ChatEventMessage) {
            return false
        }
        val chat = message.chat as? GroupChat ?: return false
        val newMembersEvent = message.chatEvent as? NewChatMembers ?: return false

        var anyUser = false

        for (member in newMembersEvent.members) {
            if (member is Bot) {
                continue
            }

            val chatMember = telegramClient.getChatMember(chat.id, member.id)

            var permissions: ChatPermissions? = null
            if (chatMember is RestrictedChatMember) {
                permissions = ChatPermissions(
                    canSendMessages = chatMember.canSendMessages,
                    canSendMediaMessages = chatMember.canSendMediaMessages,
                    canSendPolls = chatMember.canSendPolls,
                    canSendOtherMessages = chatMember.canSendOtherMessages,
                    canAddWebPagePreviews = chatMember.canAddWebpagePreviews,
                    canChangeInfo = chatMember.canChangeInfo,
                    canInviteUsers = chatMember.canInviteUsers,
                    canPinMessages = chatMember.canPinMessages
                )
            }

            telegramClient.restrictChatMember(chat.id, member.id)

            val diceMessage = telegramClient.sendDiceTo(chat.id, CubeDiceAnimationType)
            val cubeValue = diceMessage.content.dice.value

            val buttons = diceResultLimit.map {
                CallbackDataInlineKeyboardButton(it.toString(),
                    createCallbackDataForHandler(
                        encodeCallbackData(it, it == cubeValue, member.id, permissions, message.messageId),
                        CaptchaCallbackHandler::class.java
                    )
                )
            }

            val keyboard = InlineKeyboardMarkup(listOf(buttons.shuffled()))

            val kickTimeoutMinutes = botProperties.captchaKickTimeout.toMinutes()

            val captchaMessage = telegramClient.replyTextTo(
                diceMessage, """
                    Эй, ${member.usernameOrName}! Мы отобрали твою свободу слова, пока ты не тыкнешь число, выпавшее на кубике 👇
                    У тебя есть $kickTimeoutMinutes ${kickTimeoutMinutes.getForm("минута", "минуты", "минут")}
                    """.trimIndent(),
                keyboard = keyboard
            )

            captchaService.fixRestriction(chat, member, captchaMessage)

            anyUser = true
        }

        return anyUser
    }
}

@Component
class CaptchaCallbackHandler(
    private val telegramClient: TelegramClient,
    private val captchaService: CaptchaService
) : CallbackQueryHandler() {
    companion object {
        private val welcomeGif = FileId("CgACAgIAAx0CSNrJgAACAQFfXM-sSEnYFcgD6Xko5OReB_pHdgACTgADsuSgS3GU1zh-LXY2GwQ")
        private val ACCESS_RESTRICTED_MESSAGES = arrayOf("КУДА ЖМЁШЬ?!️! РУКУ УБРАЛ!", "У тебя здесь нет власти!")
        private val WRONG_ANSWER_MESSAGES = arrayOf("НЕПРАВИЛЬНЫЙ ОТВЕТ!", "Кто-то не может нажать правильную кнопочку...")
    }

    override suspend fun handleCallback(query: MessageDataCallbackQuery, data: String) {
        val message = query.message
        val chat = message.chat
        val user = query.user

        if (message !is PossiblyReplyMessage) {
            telegramClient.answerCallbackQuery(query, ACCESS_RESTRICTED_MESSAGES.random())
            return
        }
        val cubeMessage = message.replyTo

        val callbackData = parseCallbackData(data)

        if (user.id != callbackData.memberId) {
            telegramClient.answerCallbackQuery(query, ACCESS_RESTRICTED_MESSAGES.random())
            return
        }

        if (!callbackData.isRightAnswer) {
            telegramClient.answerCallbackQuery(query, WRONG_ANSWER_MESSAGES.random())
            return
        }

        if (callbackData.permissions !== null) {
            telegramClient.restoreChatMemberPermissions(chat.id, callbackData.memberId, callbackData.permissions)
        } else {
            telegramClient.restoreChatMemberPermissions(chat.id, callbackData.memberId)
        }
        captchaService.removeRestriction(chat.id, user.id)

        if (cubeMessage != null)
            telegramClient.deleteMessage(cubeMessage)
        telegramClient.deleteMessage(message)

        telegramClient.replyAnimationTo(chat.id, callbackData.replyMessage, welcomeGif)
    }
}

data class CallbackData(
    val value: Int,
    val isRightAnswer: Boolean,
    val memberId: UserId,
    val permissions: ChatPermissions?,
    val replyMessage: MessageIdentifier
)

private fun encodeCallbackData(
    value: Int,
    isRightAnswer: Boolean,
    memberId: UserId,
    permissions: ChatPermissions?,
    replyMessage: MessageIdentifier
): String {
    return "${value}/${if (isRightAnswer) "+" else "-"}/${memberId.userId}/${permissions.encode()}/${replyMessage}"
}

private fun parseCallbackData(callbackData: String): CallbackData {
    val fields = callbackData.split('/', limit = 5)

    return CallbackData(
        fields[0].toInt(),
        fields[1] == "+",
        fields[2].toInt().toChatId(),
        decodeChatPermission(fields[3]),
        fields[4].toLong()
    )
}
