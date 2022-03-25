package com.github.djaler.evilbot.entity

import javax.persistence.*

@Entity
@Table(name = "media_cache")
data class MediaCache(
    @Column
    val digest: String,

    @Column
    val fileId: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
