package com.github.djaler.evilbot.handlers.base

import com.github.djaler.evilbot.filters.message.CommandMessageFilter
import com.github.djaler.evilbot.filters.message.MessageFilter
import com.github.djaler.evilbot.filters.message.and
import dev.inmo.tgbotapi.extensions.utils.withContent
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

val spacesRegex = Regex("\\s+")

abstract class CommandHandler(
    botInfo: ExtendedBot,
    private val command: Array<String>,
    private val commandDescription: String,
    val commandScope: BotCommandScope = BotCommandScope.Default,
    filter: MessageFilter? = null
) : CommonMessageHandler(
    CommandMessageFilter(command, botInfo).let { if (filter === null) it else it and filter }
) {
    val commandInfo: BotCommand
        get() {
            return BotCommand(command.first(), commandDescription)
        }

    override suspend fun handleMessage(message: CommonMessage<*>): Boolean {
        val textMessage = message.withContent<TextContent>() ?: return false

        val args = textMessage.content.text.split(spacesRegex, limit = 2).drop(1).firstOrNull()

        handleCommand(textMessage, args)

        return true
    }

    protected abstract suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    )
}
