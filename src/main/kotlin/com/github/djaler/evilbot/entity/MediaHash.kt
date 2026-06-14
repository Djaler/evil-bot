package com.github.djaler.evilbot.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "media_hashes")
data class MediaHash(
    @Column
    val hash: Long,

    @Column
    val chatId: Short,

    @Column
    val messageId: Long,

    @Column
    val fileId: String,

    @Column
    val lastSeenAt: Instant,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
