package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.components.BackupScheduler
import com.github.djaler.evilbot.config.BackupProperties
import com.github.djaler.evilbot.handlers.base.CommandHandler
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.commands.BotCommandScopeChat
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.types.toChatId
import org.springframework.stereotype.Component

@Component
class BackupCommandHandler(
    botInfo: ExtendedBot,
    private val backupScheduler: BackupScheduler,
    private val backupProperties: BackupProperties
) : CommandHandler(
    botInfo,
    command = arrayOf("backup"),
    commandDescription = "сделать бэкап базы данных",
    commandScope = BotCommandScopeChat(backupProperties.adminTelegramId.toChatId())
) {
    override suspend fun handleCommand(message: TextMessage, args: String?) {
        if (message.chat.id.chatId != backupProperties.adminTelegramId) {
            return
        }

        backupScheduler.performBackup()
    }
}
