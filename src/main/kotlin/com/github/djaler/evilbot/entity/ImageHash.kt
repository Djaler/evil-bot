package com.github.djaler.evilbot.entity

import javax.persistence.*

@Entity
@Table(name = "image_hashes")
data class ImageHash(
    @Column
    val hash: String,

    @Column
    val chatId: Short,

    @Column
    val messageId: Long,

    @Column
    val fileId: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
