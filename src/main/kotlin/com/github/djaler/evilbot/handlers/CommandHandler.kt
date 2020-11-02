package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.message.CommandMessageFilter
import com.github.djaler.evilbot.filters.message.MessageFilter
import com.github.djaler.evilbot.filters.message.and
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import dev.inmo.tgbotapi.types.message.content.TextContent

val spacesRegex = Regex("\\s+")

abstract class CommandHandler(
    botInfo: ExtendedBot,
    private val command: Array<String>,
    private val commandDescription: String,
    filter: MessageFilter? = null
) : CommonMessageHandler(
    CommandMessageFilter(command, botInfo).let { if (filter === null) it else it and filter }
) {
    fun getCommandInfo(): BotCommand {
        return BotCommand(command.first(), commandDescription)
    }

    override suspend fun handleMessage(message: CommonMessageImpl<*>): Boolean {
        val content = message.content as? TextContent ?: return false

        val args = content.text.split(spacesRegex, limit = 2).drop(1).firstOrNull()

        handleCommand(message, args)

        return true
    }

    protected abstract suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?)
}
