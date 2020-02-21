package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.filters.ChatAdministratorFilter
import com.github.djaler.evilbot.service.BlockedStickerpackService
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.utils.createCallbackDataForHandler
import com.github.djaler.evilbot.utils.createStickerpackLink
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.HTML
import com.github.insanusmokrassar.TelegramBotAPI.types.User
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardButtons.InlineKeyboardButton
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.media.StickerContent
import com.github.insanusmokrassar.TelegramBotAPI.utils.bold
import org.springframework.stereotype.Component

@Component
class BlockStickerpackHandler(
    botInfo: User,
    private val telegramClient: TelegramClient,
    private val chatService: ChatService,
    private val blockedStickerpackService: BlockedStickerpackService,
    chatAdministratorFilter: ChatAdministratorFilter
) : CommandHandler(
    botInfo,
    command = arrayOf("block_stickerpack"),
    filter = chatAdministratorFilter
) {
    private val parseMode = HTML

    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val chat = message.chat as? PublicChat ?: return
        val replyTo = message.replyTo as? ContentMessage<*> ?: return
        val stickerContent = replyTo.content as? StickerContent ?: return

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val stickerSetName = stickerContent.media.stickerSetName
        if (stickerSetName == null) {
            telegramClient.sendTextTo(chat.id, "Стикерпак недоступен")
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

        telegramClient.sendTextTo(chat.id, responseText, parseMode = parseMode)
    }
}

@Component
class UnblockStickerpackHandler(
    botInfo: User,
    private val telegramClient: TelegramClient,
    private val chatService: ChatService,
    private val blockedStickerpackService: BlockedStickerpackService,
    chatAdministratorFilter: ChatAdministratorFilter
) : CommandHandler(
    botInfo,
    command = arrayOf("unblock_stickerpack"),
    filter = chatAdministratorFilter
) {
    private val parseMode = HTML

    override suspend fun handleCommand(message: CommonMessageImpl<*>, args: String?) {
        val chat = message.chat as? PublicChat ?: return

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val blockedStickerpacks = blockedStickerpackService.getAll(chatEntity)
        if (blockedStickerpacks.isEmpty()) {
            telegramClient.replyTextTo(
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

        telegramClient.sendTextTo(
            message.chat.id,
            text,
            keyboard = keyboard,
            parseMode = parseMode
        )
    }
}

@Component
class UnblockStickerpackCallbackHandler(
    private val telegramClient: TelegramClient,
    private val blockedStickerpackService: BlockedStickerpackService
) : CallbackQueryHandler() {
    private val parseMode = HTML

    override suspend fun handleCallback(query: MessageDataCallbackQuery, data: String) {
        val message = query.message

        val stickerpack = blockedStickerpackService.getById(data.toInt())
        if (stickerpack == null) {
            telegramClient.changeText(
                message,
                "Выбранный стикерпак ${"не был найден".bold(parseMode)} в списке заблокированных.",
                parseMode = parseMode
            )
            return
        }

        blockedStickerpackService.unblock(stickerpack)

        val packLink = createStickerpackLink(stickerpack.name, parseMode)
        telegramClient.changeText(
            message,
            "Стикерпак $packLink успешно ${"разблокирован".bold(parseMode)}.",
            parseMode = parseMode
        )
    }
}

@Component
class StickersWatchDog(
    private val telegramClient: TelegramClient,
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
            telegramClient.deleteMessage(message)
        }

        return true
    }
}
