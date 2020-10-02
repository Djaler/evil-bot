package com.github.djaler.evilbot.entity

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "captcha_restrictions")
data class CaptchaRestriction(
    @ManyToOne
    @JoinColumn(name = "chatId")
    val chat: Chat,

    @Column
    val memberTelegramId: Int,

    @Column
    val dateTime: LocalDateTime,

    @Column
    val captchaMessageId: Long,

    @Column
    val cubeMessageId: Long?,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
