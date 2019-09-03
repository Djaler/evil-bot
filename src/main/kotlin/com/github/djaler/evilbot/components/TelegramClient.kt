package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.utils.createChatPermissions
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.*
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class TelegramClient(
    private val sender: AbsSender
) {
    fun replyTextTo(
        message: Message,
        text: String,
        disableNotification: Boolean = false,
        enableMarkdown: Boolean = false
    ) {
        sender.execute(
            SendMessage(message.chatId, text)
                .apply {
                    replyToMessageId = message.messageId

                    if (disableNotification) {
                        disableNotification()
                    }

                    if (enableMarkdown) {
                        enableMarkdown(true)
                    }
                }
        )
    }

    fun replyStickerTo(message: Message, sticker: String, disableNotification: Boolean = false) {
        sender.execute(
            SendSticker()
                .apply {
                    setChatId(message.chatId)
                    setSticker(sticker)

                    replyToMessageId = message.messageId

                    if (disableNotification) {
                        disableNotification()
                    }
                }
        )
    }

    fun sendTextTo(
        chatId: Long,
        text: String,
        enableMarkdown: Boolean = false,
        keyboard: InlineKeyboardMarkup? = null
    ) {
        sender.execute(SendMessage(chatId, text).apply {
            if (enableMarkdown) {
                enableMarkdown(true)
            }
            if (keyboard != null) {
                replyMarkup = keyboard
            }
        })
    }

    fun sendStickerTo(chatId: Long, sticker: String) {
        sender.execute(SendSticker().apply {
            setChatId(chatId)
            setSticker(sticker)
        })
    }

    fun changeText(
        message: Message,
        text: String,
        enableMarkdown: Boolean = false
    ) {
        sender.execute(EditMessageText().apply {
            setChatId(message.chatId)
            messageId = message.messageId
            setText(text)

            if (enableMarkdown) {
                enableMarkdown(true)
            }
        })
    }

    fun getChatAdministrators(chatId: Long): List<ChatMember> {
        return sender.execute(GetChatAdministrators().apply {
            setChatId(chatId)
        })
    }

    fun deleteMessage(message: Message) {
        sender.execute(DeleteMessage(message.chatId, message.messageId))
    }

    fun getChatMemberPermissions(chatId: Long, memberId: Int): ChatPermissions {
        val member = sender.execute(GetChatMember().apply {
            setChatId(chatId)
            userId = memberId
        })

        return createChatPermissions(
            canSendMessages = member.canSendMessages ?: true,
            canSendMediaMessages = member.canSendMediaMessages ?: true,
            canSendPolls = member.canSendPolls ?: true,
            canSendOtherMessages = member.canSendOtherMessages ?: true,
            canAddWebPagePreviews = member.canAddWebPagePreviews ?: true
        )
    }

    fun restrictChatMember(chatId: Long, memberId: Int) {
        sender.execute(RestrictChatMember(chatId, memberId).apply {
            permissions = ChatPermissions()
        })
    }

    fun restoreChatMemberPermissions(chatId: Long, memberId: Int, permissions: ChatPermissions) {
        sender.execute(RestrictChatMember(chatId, memberId).apply {
            setPermissions(permissions)
        })
    }

    fun answerCallbackQuery(query: CallbackQuery, text: String) {
        sender.execute(AnswerCallbackQuery().apply {
            callbackQueryId = query.id
            setText(text)
        })
    }

    fun getChat(chatId: Long): Chat {
        return sender.execute(GetChat(chatId))
    }
}
