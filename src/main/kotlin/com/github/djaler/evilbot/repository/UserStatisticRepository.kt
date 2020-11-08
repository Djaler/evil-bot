package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.UserChatStatistic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserStatisticRepository : JpaRepository<UserChatStatistic, Int> {
    @Query("select s from UserChatStatistic s where s.chatId = :chatId and s.user.id = :userId")
    fun findByChatAndUser(chatId: Short, userId: Int): UserChatStatistic?

    @Query(
        "SELECT * FROM user_chat_statistics WHERE chat_id = :chatId ORDER BY messages_count DESC LIMIT :limit",
        nativeQuery = true
    )
    fun findTopByMessagesCount(chatId: Short, limit: Short): List<UserChatStatistic>

    @Query(
        "SELECT * FROM user_chat_statistics WHERE chat_id = :chatId ORDER BY last_activity DESC LIMIT :limit",
        nativeQuery = true
    )
    fun findLatest(chatId: Short, limit: Short): List<UserChatStatistic>

    fun deleteByChatId(chatsId: Short): Int
}
