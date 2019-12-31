package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.utils.fullChatPermissions
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.requests.DeleteMessage
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.InputFile
import com.github.insanusmokrassar.TelegramBotAPI.requests.answers.AnswerCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.requests.chat.get.GetChat
import com.github.insanusmokrassar.TelegramBotAPI.requests.chat.get.GetChatAdministrators
import com.github.insanusmokrassar.TelegramBotAPI.requests.chat.members.GetChatMember
import com.github.insanusmokrassar.TelegramBotAPI.requests.chat.members.KickChatMember
import com.github.insanusmokrassar.TelegramBotAPI.requests.chat.members.RestrictChatMember
import com.github.insanusmokrassar.TelegramBotAPI.requests.edit.text.EditChatMessageText
import com.github.insanusmokrassar.TelegramBotAPI.requests.send.SendMessage
import com.github.insanusmokrassar.TelegramBotAPI.requests.send.media.SendSticker
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.CallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatMember.abstracts.ChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.MarkdownParseMode
import com.github.insanusmokrassar.TelegramBotAPI.types.UserId
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.ChatPermissions
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.extended.ExtendedChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class TelegramClient(
    private val requestsExecutor: RequestsExecutor
) {
    fun replyTextTo(
        message: Message,
        text: String,
        disableNotification: Boolean = false,
        enableMarkdown: Boolean = false
    ) {
        runBlocking {
            requestsExecutor.execute(
                SendMessage(
                    chatId = message.chat.id,
                    text = text,
                    replyToMessageId = message.messageId,
                    disableNotification = disableNotification,
                    parseMode = if (enableMarkdown) MarkdownParseMode else null
                )
            )
        }
    }

    fun replyStickerTo(message: Message, sticker: InputFile, disableNotification: Boolean = false) {
        runBlocking {
            requestsExecutor.execute(
                SendSticker(
                    chatId = message.chat.id,
                    sticker = sticker,
                    replyToMessageId = message.messageId,
                    disableNotification = disableNotification
                )
            )
        }
    }

    fun sendTextTo(
        chatId: ChatId,
        text: String,
        enableMarkdown: Boolean = false,
        keyboard: InlineKeyboardMarkup? = null
    ) {
        runBlocking {
            requestsExecutor.execute(
                SendMessage(
                    chatId = chatId,
                    text = text,
                    parseMode = if (enableMarkdown) MarkdownParseMode else null,
                    replyMarkup = keyboard
                )
            )
        }
    }

    fun sendStickerTo(chatId: ChatId, sticker: InputFile) {
        runBlocking {
            requestsExecutor.execute(
                SendSticker(chatId, sticker)
            )
        }
    }

    fun changeText(
        message: Message,
        text: String,
        enableMarkdown: Boolean = false
    ) {
        runBlocking {
            requestsExecutor.execute(
                EditChatMessageText(
                    chatId = message.chat.id,
                    messageId = message.messageId,
                    text = text,
                    parseMode = if (enableMarkdown) MarkdownParseMode else null
                )
            )
        }
    }

    fun getChatAdministrators(chatId: ChatId): List<ChatMember> {
        return runBlocking {
            requestsExecutor.execute(
                GetChatAdministrators(chatId)
            )
        }
    }

    fun deleteMessage(message: Message) {
        runBlocking {
            requestsExecutor.execute(
                DeleteMessage(message.chat.id, message.messageId)
            )
        }
    }

    fun getChatMember(chatId: ChatId, memberId: UserId): ChatMember {
        return runBlocking {
            requestsExecutor.execute(
                GetChatMember(chatId, memberId)
            )
        }
    }

    fun restrictChatMember(chatId: ChatId, memberId: UserId) {
        runBlocking {
            requestsExecutor.execute(
                RestrictChatMember(chatId, memberId, permissions = ChatPermissions())
            )
        }
    }

    fun restoreChatMemberPermissions(
        chatId: ChatId,
        memberId: UserId,
        permissions: ChatPermissions = fullChatPermissions
    ) {
        runBlocking {
            requestsExecutor.execute(
                RestrictChatMember(chatId, memberId, permissions = permissions)
            )
        }
    }

    fun kickChatMember(chatId: ChatId, memberId: UserId) {
        runBlocking {
            requestsExecutor.execute(
                KickChatMember(chatId, memberId)
            )
        }
    }

    fun answerCallbackQuery(query: CallbackQuery, text: String) {
        runBlocking {
            requestsExecutor.execute(
                AnswerCallbackQuery(query.id, text)
            )
        }
    }

    fun getChat(chatId: ChatId): ExtendedChat {
        return runBlocking {
            requestsExecutor.execute(
                GetChat(chatId)
            )
        }
    }
}
