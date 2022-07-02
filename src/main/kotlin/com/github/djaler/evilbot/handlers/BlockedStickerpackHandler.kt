package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.base.CommonMessageHandler
import com.github.djaler.evilbot.service.BlockedStickerpackService
import com.github.djaler.evilbot.service.ChatService
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.asStickerContent
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import org.springframework.stereotype.Component

@Component
class BlockedStickerpackHandler(
    private val requestsExecutor: RequestsExecutor,
    private val chatService: ChatService,
    private val blockedStickerpackService: BlockedStickerpackService
) : CommonMessageHandler() {
    override val order = 0

    override suspend fun handleMessage(message: CommonMessage<*>): Boolean {
        val chat = message.chat.asPublicChat() ?: return false
        val stickerContent = message.content.asStickerContent() ?: return false

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)

        val stickerSetName = stickerContent.media.stickerSetName ?: return true

        if (blockedStickerpackService.isBlocked(stickerSetName, chatEntity)) {
            requestsExecutor.deleteMessage(message)
        }

        return true
    }
}
