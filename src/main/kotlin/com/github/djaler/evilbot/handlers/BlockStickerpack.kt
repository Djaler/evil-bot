package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.filters.message.ChatAdministratorMessageFilter
import com.github.djaler.evilbot.filters.query.ChatAdministratorCallbackQueryFilter
import com.github.djaler.evilbot.service.BlockedStickerpackService
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.utils.createCallbackDataForHandler
import com.github.djaler.evilbot.utils.createStickerpackLink
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.formatting.bold
import dev.inmo.tgbotapi.types.CallbackQuery.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.ExtendedBot
import dev.inmo.tgbotapi.types.ParseMode.HTML
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.InlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.media.StickerContent
import org.springframework.stereotype.Component

@Component
class BlockStickerpackHandler(
    botInfo: ExtendedBot,
    private val requestsExecutor: RequestsExecutor,
    private val chatService: ChatService,
    private val blockedStickerpackService: BlockedStickerpackService,
    chatAdministratorFilter: ChatAdministratorMessageFilter
) : CommandHandler(
    botInfo,
    command = arrayOf("block_stickerpack"),
    commandDescription = "заблокировать стикерпак",
    filter = chatAdministratorFilter
) {
    private val parseMode = HTML

    override suspend fun <M> handleCommand(
        message: M,
        args: String?
    ) where M : CommonMessage<TextContent>, M : FromUserMessage {
        val chat = message.chat as? PublicChat ?: return
        val replyTo = message.replyTo as? ContentMessage<*> ?: return
        val stickerContent = replyTo.content as? StickerContent ?: return

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val stickerSetName = stickerContent.media.stickerSetName
        if (stickerSetName == null) {
            requestsExecutor.sendMessage(chat.id, "Стикерпак недоступен")
            return
        }

        val (stickerpack, created) = blockedStickerpackService.getOrCreate(
            stickerSetName,
            chatEntity.id
        )

        val packLink = createStickerpackLink(stickerpack.name, parseMode)
        val responseText =
            if (created) {
                "Стикерпак $packLink успешно ${"заблокирован".bold(parseMode)}"
            } else {
                "Стикерпак $packLink ${"уже заблокирован".bold(parseMode)}"
            }

        requestsExecutor.sendMessage(chat.id, responseText, parseMode = parseMode)
    }
}

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
    filter = chatAdministratorFilter
) {
    private val parseMode = HTML

    override suspend fun <M> handleCommand(
        message: M,
        args: String?
    ) where M : CommonMessage<TextContent>, M : FromUserMessage {
        val chat = message.chat as? PublicChat ?: return

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val blockedStickerpacks = blockedStickerpackService.getAll(chatEntity)
        if (blockedStickerpacks.isEmpty()) {
            requestsExecutor.reply(
                message,
                "Заблокированных стикерпаков ${"нет".bold(parseMode)}",
                parseMode = parseMode
            )
            return
        }

        val packsLinks = arrayListOf<String>()
        val buttons = arrayListOf<InlineKeyboardButton>()

        for ((index, stickerpack) in blockedStickerpacks.withIndex()) {
            packsLinks.add("${index + 1}. ${createStickerpackLink(stickerpack.name, parseMode)}")
            buttons.add(
                CallbackDataInlineKeyboardButton(
                    (index + 1).toString(),
                    createCallbackDataForHandler(
                        stickerpack.id.toString(),
                        UnblockStickerpackCallbackHandler::class.java
                    )
                )
            )
        }

        val keyboard = InlineKeyboardMarkup(buttons.chunked(5))

        val text = """
            ${"Заблокированные стикерпаки:".bold(parseMode)}
            ${packsLinks.joinToString("\n")}

            Какой ${"разблокировать".bold(parseMode)}?""".trimIndent()

        requestsExecutor.sendMessage(
            message.chat.id,
            text,
            replyMarkup = keyboard,
            parseMode = parseMode
        )
    }
}

@Component
class UnblockStickerpackCallbackHandler(
    private val requestsExecutor: RequestsExecutor,
    private val blockedStickerpackService: BlockedStickerpackService,
    chatAdministratorFilter: ChatAdministratorCallbackQueryFilter
) : CallbackQueryHandler(filter = chatAdministratorFilter) {
    private val parseMode = HTML

    override suspend fun handleCallback(query: MessageDataCallbackQuery, data: String) {
        val message = query.message
        val userWhoClicked = query.user.usernameOrName

        val stickerpack = blockedStickerpackService.getById(data.toInt())
        if (stickerpack == null) {
            requestsExecutor.editMessageText(
                message.chat.id,
                message.messageId,
                "Выбранный стикерпак ${"не был найден".bold(parseMode)} в списке заблокированных.",
                parseMode = parseMode
            )
            return
        }

        blockedStickerpackService.unblock(stickerpack)

        val packLink = createStickerpackLink(stickerpack.name, parseMode)
        requestsExecutor.editMessageText(
            message.chat.id,
            message.messageId,
            "Стикерпак $packLink успешно ${"разблокирован".bold(parseMode)} " +
                    "администратором ${userWhoClicked.bold(parseMode)}.",
            parseMode = parseMode
        )
    }
}

@Component
class StickersWatchDog(
    private val requestsExecutor: RequestsExecutor,
    private val chatService: ChatService,
    private val blockedStickerpackService: BlockedStickerpackService
) : MessageHandler() {
    override val order = 0

    override suspend fun handleMessage(message: Message): Boolean {
        if (message !is ContentMessage<*>) {
            return false
        }
        val chat = message.chat as? PublicChat ?: return false
        val stickerContent = message.content as? StickerContent ?: return false

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val stickerSetName = stickerContent.media.stickerSetName ?: return true

        if (blockedStickerpackService.isBlocked(stickerSetName, chatEntity)) {
            requestsExecutor.deleteMessage(message)
        }

        return true
    }
}
