package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.filters.CanRestrictMemberFilter
import com.github.djaler.evilbot.service.CaptchaService
import com.github.djaler.evilbot.utils.*
import com.github.insanusmokrassar.TelegramBotAPI.types.Bot
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatMember.RestrictedChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.UserId
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.ChatPermissions
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.GroupChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.ChatEvents.NewChatMembers
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ChatEventMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.toChatId
import org.springframework.stereotype.Component

@Component
class SendCaptchaHandler(
    private val telegramClient: TelegramClient,
    private val captchaService: CaptchaService,
    private val botProperties: BotProperties,
    canRestrictMemberFilter: CanRestrictMemberFilter
) : MessageHandler(filter = canRestrictMemberFilter) {
    companion object {
        private val CAPTCHA_MESSAGES = arrayOf("Аниме - моя жизнь", "Я отдаю свою жизнь и честь Ночному Дозору")
    }

    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is ChatEventMessage) {
            return false
        }
        val chat = message.chat as? GroupChat ?: return false
        val newMembersEvent = message.chatEvent as? NewChatMembers ?: return false

        var anyUser = false;

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

            val keyboard = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        CallbackDataInlineKeyboardButton(
                            CAPTCHA_MESSAGES.random(), createCallbackDataForHandler(
                                encodeCallbackData(member.id, permissions),
                                CaptchaCallbackHandler::class.java
                            )
                        )
                    )
                )
            )

            val kickTimeoutMinutes = botProperties.captchaKickTimeout.toMinutes()

            val captchaMessage = telegramClient.sendTextTo(
                chat.id, """
                    Эй, ${member.usernameOrName}! Мы отобрали твою свободу слова, пока ты не тыкнешь сюда 👇
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
        private val ACCESS_RESTRICTED_MESSAGES = arrayOf("КУДА ЖМЁШЬ?!️! РУКУ УБРАЛ!", "У тебя здесь нет власти!")
    }

    override suspend fun handleCallback(query: MessageDataCallbackQuery, data: String) {
        val chat = query.message.chat
        val user = query.user

        val (suspectId, permissions) = parseCallbackData(data)

        if (user.id != suspectId) {
            telegramClient.answerCallbackQuery(query, ACCESS_RESTRICTED_MESSAGES.random())
            return
        }

        if (permissions !== null) {
            telegramClient.restoreChatMemberPermissions(chat.id, suspectId, permissions)
        } else {
            telegramClient.restoreChatMemberPermissions(chat.id, suspectId)
        }

        telegramClient.deleteMessage(query.message)

        captchaService.removeRestriction(chat.id, user.id)
    }
}

data class CallbackData(val memberId: UserId, val permissions: ChatPermissions?)

private fun encodeCallbackData(memberId: UserId, permissions: ChatPermissions?): String {
    return "${memberId.userId}/${permissions.encode()}"
}

private fun parseCallbackData(callbackData: String): CallbackData {
    val (memberId, permissions) = callbackData.split('/', limit = 2)

    return CallbackData(memberId.toInt().toChatId(), decodeChatPermission(permissions))
}
