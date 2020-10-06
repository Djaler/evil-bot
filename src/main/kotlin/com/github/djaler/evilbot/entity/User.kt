package com.github.djaler.evilbot.entity

import com.github.djaler.evilbot.enums.UserGender
import javax.persistence.*

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
