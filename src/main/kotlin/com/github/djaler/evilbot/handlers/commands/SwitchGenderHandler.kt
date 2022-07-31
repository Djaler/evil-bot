package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.enums.UserGender
import com.github.djaler.evilbot.handlers.base.CallbackQueryHandler
import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.createCallbackDataForHandler
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asFromUserMessage
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import org.springframework.stereotype.Component

@Component
class SwitchGenderHandler(
    private val requestsExecutor: RequestsExecutor,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("switch_gender"),
    commandDescription = "сменить гендер"
) {
    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        val user = message.asFromUserMessage()?.user ?: return

        val buttons = enumValues<UserGender>().map {
            val name = genderToName(it)

            CallbackDataInlineKeyboardButton(
                name,
                createCallbackDataForHandler(
                    SwitchGenderCallbackData(it, user.id).encode(),
                    SwitchGenderCallbackHandler::class.java
                )
            )
        }

        requestsExecutor.reply(
            message,
            "Ну выбирай",
            replyMarkup = InlineKeyboardMarkup(*buttons.toTypedArray())
        )
    }
}

@Component
class SwitchGenderCallbackHandler(
    private val requestsExecutor: RequestsExecutor,
    private val userService: UserService
) : CallbackQueryHandler() {
    override suspend fun handleCallback(query: MessageDataCallbackQuery, data: String) {
        val (selectedGender, userId) = SwitchGenderCallbackData.decode(data)

        if (selectedGender == UserGender.IT) {
            requestsExecutor.answerCallbackQuery(query, "Ты чё, ебанутый? Гендеров только два")
            return
        }

        val user = query.user

        if (user.id != userId) {
            requestsExecutor.answerCallbackQuery(query, "А тебя не спрашивали")
            return
        }

        val (userEntity, _) = userService.getOrCreateUserFrom(user)

        userService.switchGender(userEntity, selectedGender)

        val message = query.message
        requestsExecutor.editMessageText(
            message.chat.id,
            message.messageId,
            "Всё, теперь ты ${genderToName(selectedGender).lowercase()}"
        )
    }
}

data class SwitchGenderCallbackData(val gender: UserGender, val userId: UserId) {
    companion object {
        fun decode(data: String): SwitchGenderCallbackData {
            val (gender, userId) = data.split('|')

            return SwitchGenderCallbackData(
                UserGender.valueOf(gender),
                UserId(userId.toLong())
            )
        }
    }

    fun encode(): String {
        return gender.name + '|' + userId.chatId.toString()
    }
}

private fun genderToName(it: UserGender) = when (it) {
    UserGender.MALE -> "Мужчина"
    UserGender.FEMALE -> "Женщина"
    UserGender.IT -> "Другое"
}
