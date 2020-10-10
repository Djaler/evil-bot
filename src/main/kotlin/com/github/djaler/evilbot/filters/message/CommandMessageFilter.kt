package com.github.djaler.evilbot.filters.message

import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.MessageEntity.textsources.BotCommandTextSource
import dev.inmo.tgbotapi.types.message.CommonMessageImpl
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.TextContent

class CommandMessageFilter(
    private val command: Array<String>,
    botInfo: ExtendedBot
) : MessageFilter {
    private val botUsername = botInfo.username.username

    override suspend fun filter(message: Message): Boolean {
        if (message !is CommonMessageImpl<*>) {
            return false
        }
        val content = message.content as? TextContent ?: return false

        if (content.entities.none { it.source is BotCommandTextSource }) {
            return false
        }

        val command = content.text.split(" ", limit = 2).first().drop(1).split("@")
        if (command[0].toLowerCase() !in this.command) {
            return false
        }
        if (command.size > 1 && "@${command[1].toLowerCase()}" != botUsername.toLowerCase()) {
            return false
        }

        return true
    }
}
