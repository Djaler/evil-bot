package com.github.djaler.evilbot.entity

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "chats_history")
data class ChatHistory(
    @Column
    val chatId: Short,

    @Column
    val joinDate: LocalDateTime? = null,

    @Column
    val leaveDate: LocalDateTime? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
