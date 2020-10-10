package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.enums.UserGender
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.getFormByGender
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
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
        val newGender: UserGender
        if (args === null) {
            newGender = allGenders[(userEntity.gender.ordinal + 1) % allGenders.size]
        } else {
            try {
                newGender = UserGender.valueOf(args)
            } catch (e: IllegalArgumentException) {
                requestsExecutor.reply(message, "Такого гендера нет, ты все еще ${userEntity.gender.getFormByGender("мальчик", "девочка", "оно")}.\nНапиши корректно ${allGenders.joinToString { it.name }}.")
                return
            }
        }
        userService.switchGender(userEntity, newGender)
        requestsExecutor.reply(message, "Хорошо, теперь ты ${newGender.getFormByGender("мальчик", "девочка", "оно")}.")
    }
}
