package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "backup")
@ConstructorBinding
data class BackupProperties(
    val adminTelegramId: Long,
    val cron: String = "0 0 3 * * ?",
)
