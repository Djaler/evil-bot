package com.github.djaler.evilbot.utils

import org.telegram.telegrambots.meta.api.objects.ChatPermissions

val fullChatPermissions = createChatPermissions(
    canSendMessages = true,
    canSendMediaMessages = true,
    canSendPolls = true,
    canSendOtherMessages = true,
    canAddWebPagePreviews = true,
    canChangeInfo = true,
    canInviteUsers = true,
    canPinMessages = true
)

fun createChatPermissions(
    canSendMessages: Boolean,
    canSendMediaMessages: Boolean,
    canSendPolls: Boolean,
    canSendOtherMessages: Boolean,
    canAddWebPagePreviews: Boolean,
    canChangeInfo: Boolean,
    canInviteUsers: Boolean,
    canPinMessages: Boolean
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
        with(getDeclaredField("canChangeInfo")) {
            isAccessible = true
            set(permissions, canChangeInfo)
        }
        with(getDeclaredField("canInviteUsers")) {
            isAccessible = true
            set(permissions, canInviteUsers)
        }
        with(getDeclaredField("canPinMessages")) {
            isAccessible = true
            set(permissions, canPinMessages)
        }
    }

    return permissions
}

fun ChatPermissions.encode(): String {
    return listOf(
        canSendMessages,
        getCanSendMediaMessages,
        canSendPolls,
        canSendOtherMessages,
        canAddWebPagePreviews,
        canChangeInfo,
        canInviteUsers,
        canPinMessages
    ).joinToString(separator = "") { it.asSymbol().toString() }
}

fun decodeChatPermission(value: String): ChatPermissions {
    val chars = value.toCharArray()

    return createChatPermissions(
        canSendMessages = getBooleanFromSymbol(chars[0]),
        canSendMediaMessages = getBooleanFromSymbol(chars[1]),
        canSendPolls = getBooleanFromSymbol(chars[2]),
        canSendOtherMessages = getBooleanFromSymbol(chars[3]),
        canAddWebPagePreviews = getBooleanFromSymbol(chars[4]),
        canChangeInfo = getBooleanFromSymbol(chars[5]),
        canInviteUsers = getBooleanFromSymbol(chars[6]),
        canPinMessages = getBooleanFromSymbol(chars[7])
    )
}

private fun Boolean?.asSymbol() = if (this == true) '+' else '-'

private fun getBooleanFromSymbol(symbol: Char) = symbol == '+'

fun ChatPermissions.lessThan(other: ChatPermissions): Boolean {
    return (canSendMessages.isNullOrFalse() && other.canSendMessages.isTrue())
            || (getCanSendMediaMessages.isNullOrFalse() && other.getCanSendMediaMessages.isTrue())
            || (canSendPolls.isNullOrFalse() && other.canSendPolls.isTrue())
            || (canSendOtherMessages.isNullOrFalse() && other.canSendOtherMessages.isTrue())
            || (canAddWebPagePreviews.isNullOrFalse() && other.canAddWebPagePreviews.isTrue())
            || (canChangeInfo.isNullOrFalse() && other.canChangeInfo.isTrue())
            || (canInviteUsers.isNullOrFalse() && other.canInviteUsers.isTrue())
            || (canPinMessages.isNullOrFalse() && other.canPinMessages.isTrue())
}

private fun Boolean?.isNullOrFalse() = this === null || this == false
private fun Boolean?.isTrue() = this == true
