package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.filters.CanRestrictMemberFilter
import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.and
import com.github.djaler.evilbot.filters.not
import com.github.djaler.evilbot.utils.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.ChatPermissions
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@Component
@ConditionalOnProperty("evil.bot.captcha-enabled", havingValue = "true", matchIfMissing = false)
class SendCaptchaHandler(
    private val telegramClient: TelegramClient,
    canRestrictMemberFilter: CanRestrictMemberFilter
) : MessageHandler(filter = Filters.PrivateChat.not() and Filters.NewChatMember and canRestrictMemberFilter) {
    companion object {
        private val CAPTCHA_MESSAGES = arrayOf("Аниме - моя жизнь", "Я отдаю свою жизнь и честь Ночному Дозору")
    }

    override fun handleMessage(message: Message): Boolean {
        val newMembers = message.newChatMembers

        var anyUser = false;

        for (member in newMembers) {
            if (member.bot) {
                continue
            }

            val permissions = telegramClient.getChatMemberPermissions(message.chatId, member.id)

            telegramClient.restrictChatMember(message.chatId, member.id)

            val keyboard = InlineKeyboardMarkup().apply {
                keyboard = listOf(listOf(InlineKeyboardButton(CAPTCHA_MESSAGES.random()).apply {
                    callbackData = createCallbackDataForHandler(
                        encodeCallbackData(member.id, permissions),
                        CaptchaCallbackHandler::class.java
                    )
                }))
            }

            telegramClient.sendTextTo(
                message.chatId,
                "Эй, ${member.usernameOrName}! Мы отобрали твою свободу слова, пока ты не тыкнешь сюда \uD83D\uDC47",
                keyboard = keyboard
            )

            anyUser = true
        }

        return anyUser
    }
}

@Component
@ConditionalOnBean(SendCaptchaHandler::class)
class CaptchaCallbackHandler(
    private val telegramClient: TelegramClient
) : CallbackQueryHandler() {
    companion object {
        private val ACCESS_RESTRICTED_MESSAGES = arrayOf("КУДА ЖМЁШЬ?!️! РУКУ УБРАЛ!", "У тебя здесь нет власти!")
    }

    override fun handleCallback(query: CallbackQuery, data: String) {
        val user = query.from

        val (suspectId, permissions) = parseCallbackData(data)

        if (user.id != suspectId) {
            telegramClient.answerCallbackQuery(query, ACCESS_RESTRICTED_MESSAGES.random())
            return
        }

        val chat = telegramClient.getChat(query.message.chatId)

        if (permissions.lessThan(chat.permissions)) {
            telegramClient.restoreChatMemberPermissions(query.message.chatId, suspectId, permissions)
        } else {
            telegramClient.restoreChatMemberPermissions(query.message.chatId, suspectId)
        }


        telegramClient.deleteMessage(query.message)
    }
}

data class CallbackData(val memberId: Int, val permissions: ChatPermissions)

private fun encodeCallbackData(memberId: Int, permissions: ChatPermissions): String {
    return "$memberId/${permissions.encode()}"
}

private fun parseCallbackData(callbackData: String): CallbackData {
    val (memberId, permissions) = callbackData.split('/', limit = 2)

    return CallbackData(memberId.toInt(), decodeChatPermission(permissions))
}
