package com.github.djaler.evilbot.entity

import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "crowdsourcing")
data class CrowdSourcing(
    @Id
    @Column
    val chatId: Short,

    @Column
    val lastMessageTimestamp: LocalDateTime
)
