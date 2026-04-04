package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.config.BackupProperties
import com.github.djaler.evilbot.service.DatabaseBackupService
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.media.sendDocument
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BackupScheduler(
    private val databaseBackupService: DatabaseBackupService,
    private val requestsExecutor: RequestsExecutor,
    private val backupProperties: BackupProperties,
    private val sentryClient: SentryClient,
) {
    companion object {
        private val log = LogManager.getLogger()
    }

    @Scheduled(cron = "\${backup.cron}")
    fun scheduledBackup() {
        GlobalScope.launch {
            performBackup()
        }
    }

    suspend fun performBackup() {
        val chatId = backupProperties.adminTelegramId.toChatId()
        var file: java.io.File? = null

        try {
            file = databaseBackupService.createDump()
            requestsExecutor.sendDocument(chatId, document = file.asMultipartFile())
            log.info("Backup sent to admin (telegramId={})", backupProperties.adminTelegramId)
        } catch (e: Exception) {
            log.error("Failed to perform backup", e)
            sentryClient.captureException(e)

            try {
                requestsExecutor.sendMessage(chatId, "Ошибка при создании бэкапа: ${e.message}")
            } catch (sendError: Exception) {
                log.error("Failed to notify admin about backup error", sendError)
            }
        } finally {
            file?.delete()
        }
    }
}
