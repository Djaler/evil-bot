package com.github.djaler.evilbot.filters

import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.BotCommandTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent

class CommandFilter(
    private val command: Array<String>,
    botInfo: User
) : Filter {
    private val botUsername = botInfo.username!!.username

    override suspend fun filter(message: Message): Boolean {
        if (message !is CommonMessageImpl<*>) {
            return false
        }
        val content = message.content as? TextContent ?: return false

        if (content.entities.none { it.source is BotCommandTextSource }) {
            return false
        }

        val command = content.text.split(" ").first().drop(1).split("@")
        if (command[0].toLowerCase() !in this.command) {
            return false
        }
        if (command.size > 1 && "@${command[1].toLowerCase()}" != botUsername.toLowerCase()) {
            return false
        }

        return true
    }
}
