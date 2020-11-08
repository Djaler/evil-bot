package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.ChatHistory
import com.github.djaler.evilbot.model.GetOrCreateResult
import com.github.djaler.evilbot.repository.ChatHistoryRepository
import com.github.djaler.evilbot.repository.ChatRepository
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.chat.abstracts.PublicChat
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatHistoryRepository: ChatHistoryRepository
) {
    @Transactional
    fun getOrCreateChatFrom(telegramChat: PublicChat): GetOrCreateResult<Chat> {
        val chat = chatRepository.findByTelegramId(telegramChat.id.chatId)

        return if (chat != null) {
            GetOrCreateResult(chat, false)
        } else {
            GetOrCreateResult(chatRepository.save(Chat(telegramChat.id.chatId, telegramChat.title)), true)
        }
    }

    fun getChat(chatId: ChatId): Chat? {
        return chatRepository.findByTelegramId(chatId.chatId)
    }

    fun updateTitle(chat: Chat, actualTitle: String) {
        chatRepository.save(chat.copy(title = actualTitle))
    }

    fun fixChatJoin(chat: Chat) {
        chatHistoryRepository.save(ChatHistory(chat.id, joinDate = LocalDateTime.now()))
    }

    fun fixChatLeave(chat: Chat) {
        val historyEntry = chatHistoryRepository.findActiveHistoryEntry(chat.id)

        chatHistoryRepository.save(
            historyEntry?.copy(leaveDate = LocalDateTime.now())
                ?: ChatHistory(chat.id, leaveDate = LocalDateTime.now())
        )
    }

    fun getChatsLeftFor(duration: Duration): List<Short> {
        return chatHistoryRepository.findChatsLeftBefore(LocalDateTime.now().minus(duration))
    }
}
