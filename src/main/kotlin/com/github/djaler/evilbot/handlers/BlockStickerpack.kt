package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.components.TelegramLinksHelper
import com.github.djaler.evilbot.filters.ChatAdministratorFilter
import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.and
import com.github.djaler.evilbot.filters.not
import com.github.djaler.evilbot.service.BlockedStickerpackService
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.utils.createCallbackDataForHandler
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@Component
class BlockStickerpackHandler(
    botUsername: String,
    private val telegramClient: TelegramClient,
    private val telegramLinksHelper: TelegramLinksHelper,
    private val chatService: ChatService,
    private val blockedStickerpackService: BlockedStickerpackService,
    chatAdministratorFilter: ChatAdministratorFilter
) : CommandHandler(
    botUsername,
    command = arrayOf("block_stickerpack"),
    filter = Filters.PrivateChat.not() and Filters.ReplyToSticker and chatAdministratorFilter
) {
    override fun handleCommand(message: Message, args: List<String>) {
        val (chat, _) = chatService.getOrCreateChatFrom(message.chat)

        val sticker = message.replyToMessage.sticker
        if (sticker.setName == null) {
            telegramClient.sendTextTo(message.chatId, "Стикерпак недоступен")
            return
        }

        val (stickerpack, created) = blockedStickerpackService.getOrCreate(
            sticker.setName,
            chat.id
        )

        val packLink = telegramLinksHelper.createStickerpackLink(stickerpack.name)
        val responseText =
            if (created) "Стикерпак $packLink успешно *заблокирован*!" else "Стикерпак $packLink *уже заблокирован*"

        telegramClient.sendTextTo(message.chatId, responseText, enableMarkdown = true)
    }
}

@Component
class UnblockStickerpackHandler(
    botUsername: String,
    private val telegramClient: TelegramClient,
    private val telegramLinksHelper: TelegramLinksHelper,
    private val chatService: ChatService,
    private val blockedStickerpackService: BlockedStickerpackService,
    chatAdministratorFilter: ChatAdministratorFilter
) : CommandHandler(
    botUsername,
    command = arrayOf("unblock_stickerpack"),
    filter = Filters.PrivateChat.not() and chatAdministratorFilter
) {
    override fun handleCommand(message: Message, args: List<String>) {
        val (chat, _) = chatService.getOrCreateChatFrom(message.chat)

        val blockedStickerpacks = blockedStickerpackService.getAll(chat)
        if (blockedStickerpacks.isEmpty()) {
            telegramClient.replyTextTo(message, "Заблокированных стикерпаков *нет*", enableMarkdown = true)
            return
        }

        val packsLinks = arrayListOf<String>()
        val buttons = arrayListOf<InlineKeyboardButton>()

        for ((index, stickerpack) in blockedStickerpacks.withIndex()) {
            packsLinks.add("${index + 1}. ${telegramLinksHelper.createStickerpackLink(stickerpack.name)}")
            buttons.add(
                InlineKeyboardButton((index + 1).toString()).apply {
                    callbackData = createCallbackDataForHandler(
                        stickerpack.id.toString(),
                        UnblockStickerpackCallbackHandler::class.java
                    )
                }
            )
        }

        val keyboard = InlineKeyboardMarkup().apply {
            keyboard = buttons.chunked(5)
        }

        telegramClient.sendTextTo(
            message.chatId,
            "*Заблокированные стикерпаки:*\n${packsLinks.joinToString("\n")}\n\nКакой *разблокировать*?",
            keyboard = keyboard,
            enableMarkdown = true
        )
    }
}

@Component
class UnblockStickerpackCallbackHandler(
    private val telegramClient: TelegramClient,
    private val telegramLinksHelper: TelegramLinksHelper,
    private val blockedStickerpackService: BlockedStickerpackService
) : CallbackQueryHandler() {
    override fun handleCallback(query: CallbackQuery, data: String) {
        val message = query.message

        val stickerpack = blockedStickerpackService.getById(data.toInt())
        if (stickerpack == null) {
            telegramClient.changeText(
                message,
                "Выбранный стикерпак *не был найден* в списке заблокированных.",
                enableMarkdown = true
            )
            return
        }

        blockedStickerpackService.unblock(stickerpack)

        val packLink = telegramLinksHelper.createStickerpackLink(stickerpack.name)
        telegramClient.changeText(
            message,
            "Стикерпак $packLink успешно *разблокирован*.",
            enableMarkdown = true
        )
    }
}

@Component
class StickersWatchDog(
    private val telegramClient: TelegramClient,
    private val chatService: ChatService,
    private val blockedStickerpackService: BlockedStickerpackService
) : MessageHandler(filter = Filters.Sticker and Filters.PrivateChat.not()) {
    override val order = 0

    override fun handleMessage(message: Message): Boolean {
        val (chat, _) = chatService.getOrCreateChatFrom(message.chat)

        val sticker = message.sticker
        if (sticker.setName == null) {
            return true
        }

        if (blockedStickerpackService.isBlocked(message.sticker.setName, chat)) {
            telegramClient.deleteMessage(message)
        }

        return true
    }
}
