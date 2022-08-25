package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.Chat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRepository : JpaRepository<Chat, Short> {
    fun findByTelegramId(telegramId: Long): Chat?

    @Query(
        """
        SELECT chats.*
        FROM chats
        LEFT JOIN crowdsourcing ON crowdsourcing.chat_id = chats.id
        WHERE crowdsourcing.last_message_timestamp IS NULL
           OR crowdsourcing.last_message_timestamp <
              (SELECT MAX(last_activity) FROM user_chat_statistics WHERE chat_id = chats.id)
      """, nativeQuery = true
    )
    fun findChatsForCrowdSourcing(): List<Chat>
}
