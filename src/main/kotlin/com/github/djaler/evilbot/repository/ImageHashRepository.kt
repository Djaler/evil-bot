package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.ImageHash
import org.springframework.data.jpa.repository.JpaRepository

interface ImageHashRepository : JpaRepository<ImageHash, Int> {
    fun findByChatIdAndHash(chatId: Short, hash: String): ImageHash?
}
