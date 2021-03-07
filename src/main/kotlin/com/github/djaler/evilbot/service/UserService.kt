package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.User
import com.github.djaler.evilbot.entity.UserChatStatistic
import com.github.djaler.evilbot.enums.UserGender
import com.github.djaler.evilbot.model.GetOrCreateResult
import com.github.djaler.evilbot.repository.UserRepository
import com.github.djaler.evilbot.repository.UserStatisticRepository
import com.github.djaler.evilbot.utils.userId
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.types.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userStatisticRepository: UserStatisticRepository,
    private val timeService: TimeService
) {
    @Transactional
    fun getOrCreateUserFrom(telegramUser: dev.inmo.tgbotapi.types.User): GetOrCreateResult<User> {
        val user = userRepository.findByTelegramId(telegramUser.id.userId)

        return if (user != null) {
            GetOrCreateResult(user, false)
        } else {
            GetOrCreateResult(
                userRepository.save(User(telegramUser.usernameOrName, telegramUser.id.userId, UserGender.IT)),
                true
            )
        }
    }

    fun getUser(userId: UserId): User? {
        return userRepository.findByTelegramId(userId.userId)
    }

    fun updateUsername(user: User, actualUsername: String) {
        userRepository.save(user.copy(username = actualUsername))
    }

    fun switchGender(user: User, newGender: UserGender) {
        userRepository.save(user.copy(gender = newGender))
    }

    @Transactional
    fun registerMessageInStatistic(user: User, chat: Chat) {
        val statistic = userStatisticRepository.findByChatAndUser(chat.id, user.id)

        if (statistic != null) {
            userStatisticRepository.save(
                statistic.copy(
                    messagesCount = statistic.messagesCount + 1,
                    lastActivity = timeService.getServerTime()
                )
            )
        } else {
            userStatisticRepository.save(
                UserChatStatistic(
                    chat.id,
                    user,
                    messagesCount = 1,
                    lastActivity = timeService.getServerTime()
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

    fun deleteStatisticForChat(chatId: Short): Int {
        return userStatisticRepository.deleteByChatId(chatId)
    }
}
