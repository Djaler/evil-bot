package com.github.djaler.evilbot.service

import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import org.springframework.stereotype.Service

@Service
class CommandService {
    private val parentScopes = mapOf(
        BotCommandScope.AllChatAdministrators to BotCommandScope.AllGroupChats,
        BotCommandScope.AllGroupChats to BotCommandScope.Default,
        BotCommandScope.AllPrivateChats to BotCommandScope.Default,
    )

    fun normalizeCommands(commandsPerScope: Map<BotCommandScope, List<BotCommand>>): Map<BotCommandScope, List<BotCommand>> {
        return commandsPerScope.keys.associateWith { scope -> commandsForScope(scope, commandsPerScope) }
    }

    private fun commandsForScope(
        scope: BotCommandScope,
        commandsPerScope: Map<BotCommandScope, List<BotCommand>>
    ): List<BotCommand> {
        return buildList {
            var currentScope: BotCommandScope? = scope

            while (currentScope != null) {
                commandsPerScope[currentScope]?.let { addAll(it) }
                currentScope = parentScopes[currentScope]
            }
        }
    }
}
