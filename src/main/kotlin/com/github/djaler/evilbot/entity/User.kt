package com.github.djaler.evilbot.entity

import javax.persistence.*
import com.github.djaler.evilbot.enums.UserGender

@Entity
@Table(name = "users")
data class User(
    @Column
    val username: String,

    @Column
    val telegramId: Int,

    @Column
    @Enumerated(EnumType.STRING)
    val gender: UserGender,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
