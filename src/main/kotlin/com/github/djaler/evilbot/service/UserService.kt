package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.User
import com.github.djaler.evilbot.entity.UserChatStatistic
import com.github.djaler.evilbot.model.GetOrCreateResult
import com.github.djaler.evilbot.repository.UserRepository
import com.github.djaler.evilbot.repository.UserStatisticRepository
import com.github.djaler.evilbot.utils.usernameOrName
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userStatisticRepository: UserStatisticRepository
) {
    @Transactional
    fun getOrCreateUserFrom(telegramUser: org.telegram.telegrambots.meta.api.objects.User): GetOrCreateResult<User> {
        val user = userRepository.findByTelegramId(telegramUser.id)

        return if (user != null) {
            GetOrCreateResult(user, false)
        } else {
            GetOrCreateResult(userRepository.save(User(telegramUser.usernameOrName, telegramUser.id)), true)
        }
    }

    fun getUser(userId: Int): User? {
        return userRepository.findByTelegramId(userId)
    }

    fun updateUsername(user: User, actualUsername: String) {
        userRepository.save(user.copy(username = actualUsername))
    }

    @Transactional
    fun registerMessageInStatistic(user: User, chat: Chat) {
        val statistic = userStatisticRepository.findByChatAndUser(chat.id, user.id)

        if (statistic != null) {
            userStatisticRepository.save(
                statistic.copy(
                    messagesCount = statistic.messagesCount + 1,
                    lastActivity = LocalDateTime.now()
                )
            )
        } else {
            userStatisticRepository.save(
                UserChatStatistic(
                    chat.id,
                    user,
                    messagesCount = 1,
                    lastActivity = LocalDateTime.now()
                )
            )
        }
    }

    fun getStatistic(user: User, chat: Chat): UserChatStatistic? {
        return userStatisticRepository.findByChatAndUser(chat.id, user.id)
    }

    fun getTop(chat: Chat, limit: Short): List<UserChatStatistic> {
        return userStatisticRepository.findTopByMessagesCount(chat.id, limit)
    }

    fun getLatest(chat: Chat, limit: Short): List<UserChatStatistic> {
        return userStatisticRepository.findLatest(chat.id, limit)
    }
}
