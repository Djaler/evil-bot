package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.CaptchaRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import javax.transaction.Transactional

interface CaptchaRestrictionRepository : JpaRepository<CaptchaRestriction, Int> {
    @Transactional
    @Modifying
    @Query("delete from CaptchaRestriction cr where cr.chat.id = :chatId and cr.memberTelegramId = :memberId")
    fun deleteByChatAndMember(chatId: Short, memberId: Int)

    fun findByDateTimeBefore(dateTime: LocalDateTime): List<CaptchaRestriction>
}
