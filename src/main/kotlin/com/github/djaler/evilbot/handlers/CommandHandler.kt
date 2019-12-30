package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.Filter
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

abstract class CommandHandler(
    private val botUsername: String,
    private val command: Array<String>,
    private val filter: Filter? = null
) : UpdateHandler {
    override fun handleUpdate(update: Update): Boolean {
        if (!update.hasMessage()) {
            return false
        }

        val message = update.message

        if (!message.isCommand) {
            return false
        }

        val command = message.text.split(" ").first().drop(1).split("@")
        if (command[0].toLowerCase() !in this.command) {
            return false
        }
        if (command.size > 1 && command[1].toLowerCase() != botUsername.toLowerCase()) {
            return false
        }

        if (filter?.filter(update.message) == false) {
            return false
        }

        val args = message.text.split(" ").drop(1)

        handleCommand(message, args)

        return true
    }

    protected abstract fun handleCommand(message: Message, args: List<String>)
}
