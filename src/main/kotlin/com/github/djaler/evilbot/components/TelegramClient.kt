package com.github.djaler.evilbot.components

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
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

    fun restrictChatMember(chatId: Long, memberId: Int) {
        sender.execute(RestrictChatMember(chatId, memberId).apply {
            permissions = ChatPermissions()
        })
    }

    fun derestrictChatMember(chatId: Long, memberId: Int) {
        sender.execute(RestrictChatMember(chatId, memberId).apply {
            permissions = ChatPermissions()

            // ugly workaround of https://github.com/rubenlagus/TelegramBots/issues/646
            with(ChatPermissions::class.java) {
                getDeclaredField("canSendMessages").also {
                    it.isAccessible = true
                    it.set(permissions, true)
                }
                getDeclaredField("getCanSendMediaMessages").also {
                    it.isAccessible = true
                    it.set(permissions, true)
                }
                getDeclaredField("canSendPolls").also {
                    it.isAccessible = true
                    it.set(permissions, true)
                }
                getDeclaredField("canSendOtherMessages").also {
                    it.isAccessible = true
                    it.set(permissions, true)
                }
                getDeclaredField("canAddWebPagePreviews").also {
                    it.isAccessible = true
                    it.set(permissions, true)
                }
            }
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
