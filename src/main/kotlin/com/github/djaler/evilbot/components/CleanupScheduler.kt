package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import io.sentry.SentryClient
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CleanupScheduler(
    private val chatService: ChatService,
    private val userService: UserService,
    private val botProperties: BotProperties,
    private val exceptionsManager: ExceptionsManager,
    private val sentryClient: SentryClient
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    @Scheduled(cron = "0 0 0 * * ?")
    fun cleanupLeftChats() {
        try {
            val chatsIds = chatService.getChatsLeftFor(botProperties.cleanupLeftChatsTimeout)

            log.info("${chatsIds.size} old chats found")

            chatsIds.forEach {
                val deletedCount = userService.deleteStatisticForChat(it)

                val logMessage = "Deleted $deletedCount statistic entries for chat $it"
                log.info(logMessage)
                sentryClient.sendEvent(
                    EventBuilder()
                        .withMessage(logMessage)
                        .withLevel(Event.Level.INFO)
                        .build()
                )
            }
        } catch (e: Exception) {
            log.error("Error while cleanup left chats", e)

            runBlocking {
                exceptionsManager.process(e)
            }

            sentryClient.sendException(e)
        }
    }
}
