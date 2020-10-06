package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.enums.UserGender
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.getFormByGender
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import org.springframework.stereotype.Component

@Component
class SwitchGenderHandler(
    private val requestsExecutor: RequestsExecutor,
    private val userService: UserService,
    botInfo: ExtendedBot
) : CommandHandler(
    botInfo,
    command = arrayOf("switch_gender"),
    commandDescription = "сменить пол"
) {
    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val (userEntity, _) = userService.getOrCreateUserFrom(message.user)
        val allGenders = enumValues<UserGender>()
        val newGender = allGenders[(userEntity.gender.ordinal + 1) % allGenders.size]

        userService.switchGender(userEntity, newGender)
        requestsExecutor.sendMessage(message.chat, "Хорошо, теперь ты ${newGender.getFormByGender("мальчик", "девочка", "оно")}", replyToMessageId = message.messageId)
    }
}
