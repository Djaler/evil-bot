package com.github.djaler.evilbot.utils

import org.telegram.telegrambots.meta.api.objects.ChatPermissions

fun createChatPermissions(
    canSendMessages: Boolean,
    canSendMediaMessages: Boolean,
    canSendPolls: Boolean,
    canSendOtherMessages: Boolean,
    canAddWebPagePreviews: Boolean
): ChatPermissions {
    val permissions = ChatPermissions()

    // ugly workaround of https://github.com/rubenlagus/TelegramBots/issues/646
    with(ChatPermissions::class.java) {
        with(getDeclaredField("canSendMessages")) {
            isAccessible = true
            set(permissions, canSendMessages)
        }
        with(getDeclaredField("getCanSendMediaMessages")) {
            isAccessible = true
            set(permissions, canSendMediaMessages)
        }
        with(getDeclaredField("canSendPolls")) {
            isAccessible = true
            set(permissions, canSendPolls)
        }
        with(getDeclaredField("canSendOtherMessages")) {
            isAccessible = true
            set(permissions, canSendOtherMessages)
        }
        with(getDeclaredField("canAddWebPagePreviews")) {
            isAccessible = true
            set(permissions, canAddWebPagePreviews)
        }
    }

    return permissions
}

private const val CAN_SEND_MESSAGES = 1 shl 0
private const val CAN_SEND_MEDIA_MESSAGES = 1 shl 1
private const val CAN_SEND_POLLS = 1 shl 2
private const val CAN_SEND_OTHER_MESSAGES = 1 shl 4
private const val CAN_ADD_WEB_PAGE_PREVIEWS = 1 shl 8

fun ChatPermissions.encode(): Int {
    var value = 0

    if (canSendMessages) {
        value = value or CAN_SEND_MESSAGES
    }
    if (getCanSendMediaMessages) {
        value = value or CAN_SEND_MEDIA_MESSAGES
    }
    if (canSendPolls) {
        value = value or CAN_SEND_POLLS
    }
    if (canSendOtherMessages) {
        value = value or CAN_SEND_OTHER_MESSAGES
    }
    if (canAddWebPagePreviews) {
        value = value or CAN_ADD_WEB_PAGE_PREVIEWS
    }

    return value
}

fun decodeChatPermission(value: Int): ChatPermissions {
    return createChatPermissions(
        canSendMessages = value and CAN_SEND_MESSAGES > 0,
        canSendMediaMessages = value and CAN_SEND_MEDIA_MESSAGES > 0,
        canSendPolls = value and CAN_SEND_POLLS > 0,
        canSendOtherMessages = value and CAN_SEND_OTHER_MESSAGES > 0,
        canAddWebPagePreviews = value and CAN_ADD_WEB_PAGE_PREVIEWS > 0
    )
}
