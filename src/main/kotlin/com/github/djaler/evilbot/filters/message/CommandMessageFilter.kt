package com.github.djaler.evilbot.filters.message

import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.MessageEntity.textsources.BotCommandTextSource
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.TextContent

class CommandMessageFilter(
    private val command: Array<String>,
    botInfo: ExtendedBot
) : MessageFilter {
    private val botUsername = botInfo.username.username

    override suspend fun filter(message: Message): Boolean {
        if (message !is CommonMessage<*>) {
            return false
        }
        val content = message.content as? TextContent ?: return false

        if (content.textSources.none { it is BotCommandTextSource }) {
            return false
        }

        val command = content.text.split(" ", limit = 2).first().drop(1).split("@")
        if (command[0].lowercase() !in this.command) {
            return false
        }
        if (command.size > 1 && "@${command[1].lowercase()}" != botUsername.lowercase()) {
            return false
        }

        return true
    }
}
