package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.model.GetOrCreateResult
import com.github.djaler.evilbot.repository.ChatRepository
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.PublicChat
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository
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
}
