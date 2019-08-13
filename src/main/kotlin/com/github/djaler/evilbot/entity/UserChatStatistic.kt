package com.github.djaler.evilbot.entity

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "user_chat_statistics")
data class UserChatStatistic(
    @Column
    val chatId: Short,

    @ManyToOne
    @JoinColumn(name = "userId")
    val user: User,

    @Column
    val messagesCount: Int,

    @Column
    val lastActivity: LocalDateTime,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
