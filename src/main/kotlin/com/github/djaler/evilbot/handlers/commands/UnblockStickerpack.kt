package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.filters.message.ChatAdministratorMessageFilter
import com.github.djaler.evilbot.filters.query.ChatAdministratorCallbackQueryFilter
import com.github.djaler.evilbot.handlers.base.CallbackQueryHandler
import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.service.BlockedStickerpackService
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.utils.createCallbackDataForHandler
import com.github.djaler.evilbot.utils.stickerpackLink
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.formatting.bold
import dev.inmo.tgbotapi.extensions.utils.formatting.boldln
import dev.inmo.tgbotapi.extensions.utils.formatting.buildEntities
import dev.inmo.tgbotapi.extensions.utils.formatting.newLine
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import org.springframework.stereotype.Component

@Component
class UnblockStickerpackHandler(
    botInfo: ExtendedBot,
    private val requestsExecutor: RequestsExecutor,
    private val chatService: ChatService,
    private val blockedStickerpackService: BlockedStickerpackService,
    chatAdministratorFilter: ChatAdministratorMessageFilter
) : CommandHandler(
    botInfo,
    command = arrayOf("unblock_stickerpack"),
    commandDescription = "разблокировать стикерпак",
    commandScope = BotCommandScope.AllChatAdministrators,
    filter = chatAdministratorFilter
) {
    override suspend fun handleCommand(
        message: CommonMessage<TextContent>,
        args: String?
    ) {
        val chat = message.chat.asPublicChat() ?: return

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val blockedStickerpacks = blockedStickerpackService.getAll(chatEntity)
        if (blockedStickerpacks.isEmpty()) {
            requestsExecutor.reply(
                message,
                buildEntities { +"Заблокированных стикерпаков " + bold("нет") },
            )
            return
        }

        val buttons = blockedStickerpacks.mapIndexed { index, stickerpack ->
            CallbackDataInlineKeyboardButton(
                (index + 1).toString(),
                createCallbackDataForHandler(
                    stickerpack.id.toString(),
                    UnblockStickerpackCallbackHandler::class.java
                )
            )
        }

        val keyboard = InlineKeyboardMarkup(buttons.chunked(5))

        val text = buildEntities {
            boldln("Заблокированные стикерпаки:")

            for ((index, stickerpack) in blockedStickerpacks.withIndex()) {
                +"${index + 1}." + stickerpackLink(stickerpack.name) + newLine
            }

            +"Какой " + bold("разблокировать") + "?"
        }
        requestsExecutor.sendMessage(
            message.chat.id,
            text,
            replyMarkup = keyboard,
        )
    }
}

@Component
class UnblockStickerpackCallbackHandler(
    private val requestsExecutor: RequestsExecutor,
    private val blockedStickerpackService: BlockedStickerpackService,
    chatAdministratorFilter: ChatAdministratorCallbackQueryFilter
) : CallbackQueryHandler(filter = chatAdministratorFilter) {
    override suspend fun handleCallback(query: MessageDataCallbackQuery, data: String) {
        val message = query.message
        val userWhoClicked = query.user.usernameOrName

        val stickerpack = blockedStickerpackService.getById(data.toInt())
        if (stickerpack == null) {
            requestsExecutor.editMessageText(
                message.chat.id,
                message.messageId,
                buildEntities { +"Выбранный стикерпак " + bold("не был найден") + " в списке заблокировванных" },
            )
            return
        }

        blockedStickerpackService.unblock(stickerpack)

        requestsExecutor.editMessageText(
            message.chat.id,
            message.messageId,
            buildEntities(separator = " ") {
                +"Стикерпак" + stickerpackLink(stickerpack.name)
                +"успешно" + bold("разблокирован")
                +"администратором " + bold(userWhoClicked)
            }
        )
    }
}
