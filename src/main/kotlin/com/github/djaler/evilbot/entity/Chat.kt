package com.github.djaler.evilbot.entity

import javax.persistence.*

@Entity
@Table(name = "chats")
data class Chat(
    @Column
    val telegramId: Long,

    @Column
    val title: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Short = 0
)
