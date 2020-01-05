package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.Filter
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.BotCommandTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent

abstract class CommandHandler(
    private val botInfo: User,
    private val command: Array<String>,
    private val filter: Filter? = null
) : CommonMessageHandler(filter) {
    override suspend fun handleMessage(message: CommonMessageImpl<*>): Boolean {
        val content = message.content as? TextContent ?: return false

        if (content.entities.none { it.source is BotCommandTextSource }) {
            return false
        }

        val command = content.text.split(" ").first().drop(1).split("@")
        if (command[0].toLowerCase() !in this.command) {
            return false
        }
        if (command.size > 1 && "@${command[1].toLowerCase()}" != botInfo.username!!.username.toLowerCase()) {
            return false
        }

        val args = content.text.split(" ").drop(1)

        handleCommand(message, args)

        return true
    }

    protected abstract suspend fun handleCommand(message: CommonMessageImpl<*>, args: List<String>)
}
