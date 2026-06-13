package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.MediaHash
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MediaHashRepository : JpaRepository<MediaHash, Long> {
    /**
     * '#' — XOR в Postgres, bit_count — число единичных битов (PG14+).
     * Линейный скан внутри чата.
     */
    @Query(
        value = "SELECT * FROM media_hashes WHERE chat_id = :chatId AND bit_count(hash # :hash) <= :maxDistance ORDER BY id LIMIT 1",
        nativeQuery = true
    )
    fun findByChatIdAndHashCloseTo(
        @Param("chatId") chatId: Short,
        @Param("hash") hash: Long,
        @Param("maxDistance") maxDistance: Int
    ): MediaHash?
}
