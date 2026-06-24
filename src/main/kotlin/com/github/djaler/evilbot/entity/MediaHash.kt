package com.github.djaler.evilbot.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
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

    @Column(name = "duration")
    val durationSeconds: Long? = null,

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "frame_hashes", columnDefinition = "bigint[]")
    val frameHashes: List<Long>? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
