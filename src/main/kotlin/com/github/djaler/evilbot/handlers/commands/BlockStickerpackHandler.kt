package com.github.djaler.evilbot.handlers.commands

import com.github.djaler.evilbot.filters.message.ChatAdministratorMessageFilter
import com.github.djaler.evilbot.handlers.base.CommandHandler
import com.github.djaler.evilbot.service.BlockedStickerpackService
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.utils.stickerpackLink
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.asStickerContent
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.bold
import dev.inmo.tgbotapi.utils.buildEntities
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
    commandScope = BotCommandScope.AllChatAdministrators,
    filter = chatAdministratorFilter
) {
    override suspend fun handleCommand(
        message: TextMessage,
        args: String?
    ) {
        val chat = message.chat.asPublicChat() ?: return
        val stickerContent = message.replyTo?.asContentMessage()?.content?.asStickerContent() ?: return

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

        val responseText = buildEntities(separator = " ") {
            +"Стикерпак" + stickerpackLink(stickerpack.name)
            if (created) {
                +"успешно" + bold("заблокирован")
            } else {
                bold("уже заблокирован")
            }
        }

        requestsExecutor.sendMessage(chat.id, responseText)
    }
}
