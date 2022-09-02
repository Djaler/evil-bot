package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.CrowdSourcing
import com.github.djaler.evilbot.repository.ChatRepository
import com.github.djaler.evilbot.repository.CrowdSourcingRepository
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime

//@Component
class CrowdSourcingScheduler(
    private val requestsExecutor: RequestsExecutor,
    private val sentryClient: SentryClient,
    private val chatRepository: ChatRepository,
    private val crowdSourcingRepository: CrowdSourcingRepository
) {
    companion object {
        private val MESSAGE = """
                Привет, это автор Злого бота. 
                Недавно появилась информация, что Heroku, где хостится бот, скоро закроет бесплатные тарифы https://habr.com/ru/news/t/684720/
                Было принято решение переехать на другое облачное решение или VPS.
                Для этого открываю сбор средств на оплату https://www.tinkoff.ru/cf/A69Q6QMartK
                Спасибо каждому, кто впишется
                """.trimIndent()

        private val log = LogManager.getLogger()
    }

    @Scheduled(cron = "0 0 17 * * 5")
    fun sendMessages() {
        val chats = chatRepository.findChatsForCrowdSourcing()

        GlobalScope.launch {
            chats.forEach {
                try {
                    sendMessage(it)
                } catch (e: Exception) {
                    log.error("Failed to send message to chat ${it.id}", e)
                    sentryClient.setExtra("chat", it.toString())
                    sentryClient.captureException(e)
                }
            }
        }
    }

    private suspend fun sendMessage(chat: Chat) {
        requestsExecutor.sendMessage(chat.telegramId.toChatId(), MESSAGE)

        val entity = crowdSourcingRepository.findByIdOrNull(chat.id)
        val now = LocalDateTime.now()
        crowdSourcingRepository.save(
            entity?.copy(lastMessageTimestamp = now)
                ?: CrowdSourcing(chat.id, now)
        )
    }
}
