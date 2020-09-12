package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.utils.fullChatPermissions
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.answers.answerCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.get.getChat
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.get.getChatAdministrators
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.members.getChatMember
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.members.kickChatMember
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.chat.members.restrictChatMember
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.deleteMessage
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.edit.text.editMessageText
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendAnimation
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendSticker
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.polls.sendQuizPoll
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.polls.sendRegularPoll
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.InputFile
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
import com.github.insanusmokrassar.TelegramBotAPI.types.polls.QuizPoll
import com.github.insanusmokrassar.TelegramBotAPI.types.polls.RegularPoll
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
        requestsExecutor.sendMessage(
            chat = message.chat,
            text = text,
            replyToMessageId = message.messageId,
            disableNotification = disableNotification,
            parseMode = parseMode
        )
    }

    suspend fun replyStickerTo(message: Message, sticker: InputFile, disableNotification: Boolean = false) {
        requestsExecutor.sendSticker(
            chatId = message.chat.id,
            sticker = sticker,
            replyToMessageId = message.messageId,
            disableNotification = disableNotification
        )
    }

    suspend fun replyAnimationTo(
        chatId: ChatId,
        messageId: MessageIdentifier,
        animation: InputFile,
        disableNotification: Boolean = false
    ) {
        requestsExecutor.sendAnimation(
            chatId = chatId,
            animation = animation,
            replyToMessageId = messageId,
            disableNotification = disableNotification
        )
    }

    suspend fun replyPollTo(message: Message, poll: RegularPoll) {
        requestsExecutor.sendRegularPoll(
            chat = message.chat,
            poll = poll
        )
    }

    suspend fun replyPollTo(message: Message, poll: QuizPoll) {
        requestsExecutor.sendQuizPoll(
            chat = message.chat,
            quizPoll = poll
        )
    }

    suspend fun sendTextTo(
        chatId: ChatId,
        text: String,
        parseMode: ParseMode? = null,
        keyboard: InlineKeyboardMarkup? = null
    ): ContentMessage<TextContent> {
        return requestsExecutor.sendMessage(chatId, text, parseMode, replyMarkup = keyboard)
    }

    suspend fun sendStickerTo(chatId: ChatId, sticker: InputFile) {
        requestsExecutor.sendSticker(chatId, sticker)
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
        requestsExecutor.editMessageText(chatId, messageId, text, parseMode)
    }

    suspend fun getChatAdministrators(chatId: ChatId): List<ChatMember> {
        return requestsExecutor.getChatAdministrators(chatId)
    }

    suspend fun deleteMessage(message: Message) {
        requestsExecutor.deleteMessage(message)
    }

    suspend fun getChatMember(chatId: ChatId, memberId: UserId): ChatMember {
        return requestsExecutor.getChatMember(chatId, memberId)
    }

    suspend fun restrictChatMember(chatId: ChatId, memberId: UserId) {
        requestsExecutor.restrictChatMember(chatId, memberId)
    }

    suspend fun restoreChatMemberPermissions(
        chatId: ChatId,
        memberId: UserId,
        permissions: ChatPermissions = fullChatPermissions
    ) {
        requestsExecutor.restrictChatMember(chatId, memberId, permissions = permissions)
    }

    suspend fun kickChatMember(chatId: ChatId, memberId: UserId) {
        requestsExecutor.kickChatMember(chatId, memberId)
    }

    suspend fun answerCallbackQuery(query: CallbackQuery, text: String) {
        requestsExecutor.answerCallbackQuery(query, text)
    }

    suspend fun getChat(chatId: ChatId): ExtendedChat {
        return requestsExecutor.getChat(chatId)
    }
}
