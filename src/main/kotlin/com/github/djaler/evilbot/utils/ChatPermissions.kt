package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.types.chat.ChatPermissions

val fullChatPermissions = ChatPermissions(
    canSendMessages = true,
    canSendMediaMessages = true,
    canSendPolls = true,
    canSendOtherMessages = true,
    canAddWebPagePreviews = true,
    canChangeInfo = true,
    canInviteUsers = true,
    canPinMessages = true
)

fun ChatPermissions?.encode(): String {
    if (this === null) {
        return ""
    }
    return listOf(
        canSendMessages,
        canSendMediaMessages,
        canSendPolls,
        canSendOtherMessages,
        canAddWebPagePreviews,
        canChangeInfo,
        canInviteUsers,
        canPinMessages
    ).joinToString(separator = "") { it.asSymbol().toString() }
}

fun decodeChatPermission(value: String): ChatPermissions? {
    if (value.isEmpty()) {
        return null
    }

    val chars = value.toCharArray()

    return ChatPermissions(
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
