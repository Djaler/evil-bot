package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.DicePollCaptchaRestriction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface DicePollCaptchaRestrictionRepository : JpaRepository<DicePollCaptchaRestriction, Int> {
    fun findByDateTimeBefore(dateTime: LocalDateTime): List<DicePollCaptchaRestriction>

    fun findByPollId(pollId: String): DicePollCaptchaRestriction?
}
