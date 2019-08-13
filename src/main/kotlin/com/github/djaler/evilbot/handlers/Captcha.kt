package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.and
import com.github.djaler.evilbot.filters.not
import com.github.djaler.evilbot.utils.createCallbackDataForHandler
import com.github.djaler.evilbot.utils.usernameOrName
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@Component
@ConditionalOnProperty("evil.bot.captcha-enabled", havingValue = "true", matchIfMissing = false)
class SendCaptchaHandler(
    private val telegramClient: TelegramClient
) : MessageHandler(filter = Filters.PrivateChat.not() and Filters.NewChatMember) {
    companion object {
        private val CAPTCHA_MESSAGES = arrayOf("Аниме - моя жизнь", "Я отдаю свою жизнь и честь Ночному Дозору")
    }

    override fun handleMessage(message: Message): Boolean {
        val newMembers = message.newChatMembers

        for (member in newMembers) {
            if (member.bot) {
                continue
            }

            telegramClient.restrictChatMember(message.chatId, member.id)

            val keyboard = InlineKeyboardMarkup().apply {
                keyboard = listOf(listOf(InlineKeyboardButton(CAPTCHA_MESSAGES.random()).apply {
                    callbackData = createCallbackDataForHandler(
                        member.id.toString(),
                        CaptchaCallbackHandler::class.java
                    )
                }))
            }

            telegramClient.sendTextTo(
                message.chatId,
                "Эй, ${member.usernameOrName}! Мы отобрали твою свободу слова, пока ты не тыкнешь сюда \uD83D\uDC47",
                keyboard = keyboard
            )
        }

        return true
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

        val suspectId = data.toInt()

        if (user.id != suspectId) {
            telegramClient.answerCallbackQuery(query, ACCESS_RESTRICTED_MESSAGES.random())
            return
        }

        telegramClient.derestrictChatMember(query.message.chatId, suspectId)

        telegramClient.deleteMessage(query.message)
    }
}
