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
import com.github.insanusmokrassar.TelegramBotAPI.requests.send.SendTextMessage
import com.github.insanusmokrassar.TelegramBotAPI.requests.send.media.SendSticker
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.CallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatMember.abstracts.ChatMember
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.ParseMode
import com.github.insanusmokrassar.TelegramBotAPI.types.UserId
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.ChatPermissions
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.extended.ExtendedChat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
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
        parseMode: ParseMode? = null
    ) {
        requestsExecutor.execute(
            SendTextMessage(
                chatId = message.chat.id,
                text = text,
                replyToMessageId = message.messageId,
                disableNotification = disableNotification,
                parseMode = parseMode
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
        parseMode: ParseMode? = null,
        keyboard: InlineKeyboardMarkup? = null
    ): ContentMessage<TextContent> {
        return requestsExecutor.execute(
            SendTextMessage(
                chatId = chatId,
                text = text,
                parseMode = parseMode,
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
        parseMode: ParseMode? = null
    ) {
        changeText(message.chat.id, message.messageId, text, parseMode)
    }

    suspend fun changeText(
        chatId: ChatId,
        messageId: MessageIdentifier,
        text: String,
        parseMode: ParseMode? = null
    ) {
        requestsExecutor.execute(
            EditChatMessageText(
                chatId = chatId,
                messageId = messageId,
                text = text,
                parseMode = parseMode
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
