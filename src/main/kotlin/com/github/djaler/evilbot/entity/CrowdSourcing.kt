package com.github.djaler.evilbot.entity

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "crowdsourcing")
data class CrowdSourcing(
    @Id
    @Column
    val chatId: Short,

    @Column
    val lastMessageTimestamp: LocalDateTime
)
