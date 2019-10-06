package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.model.GetOrCreateResult
import com.github.djaler.evilbot.repository.ChatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository
) {
    @Transactional
    fun getOrCreateChatFrom(telegramChat: org.telegram.telegrambots.meta.api.objects.Chat): GetOrCreateResult<Chat> {
        val chat = chatRepository.findByTelegramId(telegramChat.id)

        return if (chat != null) {
            GetOrCreateResult(chat, false)
        } else {
            GetOrCreateResult(chatRepository.save(Chat(telegramChat.id, telegramChat.title)), true)
        }
    }

    fun getChat(chatId: Long): Chat? {
        return chatRepository.findByTelegramId(chatId)
    }

    fun updateTitle(chat: Chat, actualTitle: String) {
        chatRepository.save(chat.copy(title = actualTitle))
    }
}
