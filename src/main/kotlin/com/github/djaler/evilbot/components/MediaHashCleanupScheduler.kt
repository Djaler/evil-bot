package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.config.BotProperties
import com.github.djaler.evilbot.service.DuplicateMediaChecker
import org.apache.logging.log4j.LogManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class MediaHashCleanupScheduler(
    private val duplicateMediaChecker: DuplicateMediaChecker,
    private val botProperties: BotProperties,
    private val sentryClient: SentryClient,
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    @Scheduled(cron = "\${evil.bot.media-hash-cleanup-cron:0 0 4 * * ?}")
    fun cleanup() {
        try {
            val threshold = Instant.now().minus(botProperties.mediaHashTtl)
            val deleted = duplicateMediaChecker.deleteOlderThan(threshold)

            log.info("Deleted $deleted media hashes older than ${botProperties.mediaHashTtl}")
        } catch (e: Exception) {
            log.error("Error while cleaning up media hashes", e)

            sentryClient.captureException(e)
        }
    }
}
