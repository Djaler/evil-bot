package com.github.djaler.evilbot.entity

import javax.persistence.*

@Entity
@Table(name = "blocked_stickerpacks")
data class BlockedStickerpack(
    @Column
    val chatId: Short,

    @Column
    val name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
