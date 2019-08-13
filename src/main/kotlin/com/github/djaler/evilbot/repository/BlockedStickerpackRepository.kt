package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.BlockedStickerpack
import org.springframework.data.jpa.repository.JpaRepository

interface BlockedStickerpackRepository : JpaRepository<BlockedStickerpack, Int> {
    fun findByChatId(chatId: Short): List<BlockedStickerpack>
    fun findByNameAndChatId(name: String, chatId: Short): BlockedStickerpack?
    fun existsByNameAndChatId(name: String, chatId: Short): Boolean
}
