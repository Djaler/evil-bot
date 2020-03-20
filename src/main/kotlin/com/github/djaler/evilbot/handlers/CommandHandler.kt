package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.CommandFilter
import com.github.djaler.evilbot.filters.Filter
import com.github.djaler.evilbot.filters.and
import com.github.insanusmokrassar.TelegramBotAPI.types.ExtendedBot
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent

abstract class CommandHandler(
    botInfo: ExtendedBot,
    command: Array<String>,
    filter: Filter? = null
) : CommonMessageHandler(
    CommandFilter(command, botInfo).let { if (filter === null) it else it and filter }
) {
    override suspend fun handleMessage(message: CommonMessageImpl<*>): Boolean {
        val content = message.content as? TextContent ?: return false

        val args = content.text.split(" ", limit = 2).drop(1).firstOrNull()

        handleCommand(message, args)

        return true
    }

    protected abstract suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?)
}
