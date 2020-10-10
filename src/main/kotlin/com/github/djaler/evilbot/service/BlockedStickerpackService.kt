package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.BlockedStickerpack
import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.model.GetOrCreateResult
import com.github.djaler.evilbot.repository.BlockedStickerpackRepository
import dev.inmo.tgbotapi.types.StickerSetName
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockedStickerpackService(
    private val blockedStickerpackRepository: BlockedStickerpackRepository
) {
    @Transactional
    fun getOrCreate(stickerpackName: StickerSetName, chatId: Short): GetOrCreateResult<BlockedStickerpack> {
        val stickerpack = blockedStickerpackRepository.findByNameAndChatId(stickerpackName, chatId)

        return if (stickerpack != null) {
            GetOrCreateResult(stickerpack, false)
        } else {
            GetOrCreateResult(blockedStickerpackRepository.save(BlockedStickerpack(chatId, stickerpackName)), true)
        }
    }

    fun getAll(chat: Chat): List<BlockedStickerpack> {
        return blockedStickerpackRepository.findByChatId(chat.id)
    }

    fun getById(id: Int): BlockedStickerpack? {
        return blockedStickerpackRepository.findByIdOrNull(id)
    }

    fun unblock(stickerpack: BlockedStickerpack) {
        blockedStickerpackRepository.delete(stickerpack)
    }

    fun isBlocked(stickerpackName: StickerSetName, chat: Chat): Boolean {
        return blockedStickerpackRepository.existsByNameAndChatId(stickerpackName, chat.id)
    }
}
