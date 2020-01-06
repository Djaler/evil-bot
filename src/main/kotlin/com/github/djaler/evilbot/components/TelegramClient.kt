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
import org.springframework.stereotype.Component

@Component
@RecordBreadcrumb
class TelegramClient(
    private val requestsExecutor: RequestsExecutor
) {
    suspend fun replyTextTo(
        message: Message,
        text: String,
        disableNotification: Boolean = false,
        enableMarkdown: Boolean = false
    ) {
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

    suspend fun replyStickerTo(message: Message, sticker: InputFile, disableNotification: Boolean = false) {
        requestsExecutor.execute(
            SendSticker(
                chatId = message.chat.id,
                sticker = sticker,
                replyToMessageId = message.messageId,
                disableNotification = disableNotification
            )
        )
    }

    suspend fun sendTextTo(
        chatId: ChatId,
        text: String,
        enableMarkdown: Boolean = false,
        keyboard: InlineKeyboardMarkup? = null
    ) {
        requestsExecutor.execute(
            SendMessage(
                chatId = chatId,
                text = text,
                parseMode = if (enableMarkdown) MarkdownParseMode else null,
                replyMarkup = keyboard
            )
        )
    }

    suspend fun sendStickerTo(chatId: ChatId, sticker: InputFile) {
        requestsExecutor.execute(
            SendSticker(chatId, sticker)
        )
    }

    suspend fun changeText(
        message: Message,
        text: String,
        enableMarkdown: Boolean = false
    ) {
        requestsExecutor.execute(
            EditChatMessageText(
                chatId = message.chat.id,
                messageId = message.messageId,
                text = text,
                parseMode = if (enableMarkdown) MarkdownParseMode else null
            )
        )
    }

    suspend fun getChatAdministrators(chatId: ChatId): List<ChatMember> {
        return requestsExecutor.execute(
            GetChatAdministrators(chatId)
        )
    }

    suspend fun deleteMessage(message: Message) {
        requestsExecutor.execute(
            DeleteMessage(message.chat.id, message.messageId)
        )
    }

    suspend fun getChatMember(chatId: ChatId, memberId: UserId): ChatMember {
        return requestsExecutor.execute(
            GetChatMember(chatId, memberId)
        )
    }

    suspend fun restrictChatMember(chatId: ChatId, memberId: UserId) {
        requestsExecutor.execute(
            RestrictChatMember(chatId, memberId, permissions = ChatPermissions())
        )
    }

    suspend fun restoreChatMemberPermissions(
        chatId: ChatId,
        memberId: UserId,
        permissions: ChatPermissions = fullChatPermissions
    ) {
        requestsExecutor.execute(
            RestrictChatMember(chatId, memberId, permissions = permissions)
        )
    }

    suspend fun kickChatMember(chatId: ChatId, memberId: UserId) {
        requestsExecutor.execute(
            KickChatMember(chatId, memberId)
        )
    }

    suspend fun answerCallbackQuery(query: CallbackQuery, text: String) {
        requestsExecutor.execute(
            AnswerCallbackQuery(query.id, text)
        )
    }

    suspend fun getChat(chatId: ChatId): ExtendedChat {
        return requestsExecutor.execute(
            GetChat(chatId)
        )
    }
}
