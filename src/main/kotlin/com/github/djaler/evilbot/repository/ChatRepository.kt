package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.Chat
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Short> {
    fun findByTelegramId(telegramId: Long): Chat?
}
