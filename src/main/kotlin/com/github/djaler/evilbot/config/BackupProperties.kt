package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "backup")
data class BackupProperties(
    val adminTelegramId: Long,
    val cron: String = "0 0 3 * * ?",
)
