package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.ChatHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface ChatHistoryRepository : JpaRepository<ChatHistory, Int> {
    @Query("select ch from ChatHistory ch where ch.chatId = ?1 and ch.leaveDate is null")
    fun findActiveHistoryEntry(chatId: Short): ChatHistory?

    @Query(
        """select ch.chatId from ChatHistory ch
                where ch.joinDate < ch.leaveDate
                    or ch.joinDate is null
                group by ch.chatId
                having max(ch.leaveDate) < :date"""
    )
    fun findChatsLeftBefore(date: LocalDateTime): List<Short>
}
