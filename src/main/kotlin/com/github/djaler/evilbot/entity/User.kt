package com.github.djaler.evilbot.entity

import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Column
    val username: String,

    @Column
    val telegramId: Int,

    @Column
    val male: Boolean = true,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
