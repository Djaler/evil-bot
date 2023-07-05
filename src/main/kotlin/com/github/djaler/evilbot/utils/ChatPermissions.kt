package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.types.chat.ChatPermissions
import dev.inmo.tgbotapi.types.chat.member.RestrictedChatMember

val RestrictedChatMember.chatPermissions: ChatPermissions
    get() = ChatPermissions(
        canSendMessages = canSendMessages,
        canSendMediaMessages = canSendMediaMessages,
        canSendPolls = canSendPolls,
        canSendOtherMessages = canSendOtherMessages,
        canAddWebPagePreviews = canAddWebpagePreviews,
        canChangeInfo = canChangeInfo,
        canInviteUsers = canInviteUsers,
        canPinMessages = canPinMessages
    )
