package com.github.djaler.evilbot.entity

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "dice_poll_captcha_restrictions")
data class DicePollCaptchaRestriction(
    @ManyToOne
    @JoinColumn(name = "chatId")
    val chat: Chat,

    @Column
    val memberTelegramId: Long,

    @Column
    val dateTime: LocalDateTime,

    @Column
    val joinMessageId: Long,

    @Column
    val diceMessageId: Long,

    @Column
    val pollMessageId: Long,

    @Column
    val pollId: String,

    @Column
    val correctAnswerIndex: Int,

    @Column
    val canSendMessages: Boolean,

    @Column
    val canSendMediaMessages: Boolean,

    @Column
    val canSendPolls: Boolean,

    @Column
    val canSendOtherMessages: Boolean,

    @Column
    val canAddWebPagePreviews: Boolean,

    @Column
    val canChangeInfo: Boolean,

    @Column
    val canInviteUsers: Boolean,

    @Column
    val canPinMessages: Boolean,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
