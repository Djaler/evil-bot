package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.DicePollCaptchaRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface DicePollCaptchaRestrictionRepository : JpaRepository<DicePollCaptchaRestriction, Int> {
    fun findByDateTimeBefore(dateTime: LocalDateTime): List<DicePollCaptchaRestriction>

    fun findByPollId(pollId: String): DicePollCaptchaRestriction?

    @Query("select d from DicePollCaptchaRestriction d where d.chat.telegramId = :chatTelegramId and d.memberTelegramId = :memberTelegramId")
    fun findByChatTelegramIdAndMemberTelegramId(
        chatTelegramId: Long,
        memberTelegramId: Long
    ): DicePollCaptchaRestriction?
}
