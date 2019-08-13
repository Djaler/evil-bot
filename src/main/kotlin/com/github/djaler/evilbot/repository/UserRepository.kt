package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Int> {
    fun findByTelegramId(telegramId: Int): User?
}
