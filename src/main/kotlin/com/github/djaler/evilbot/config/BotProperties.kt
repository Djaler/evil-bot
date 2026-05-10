package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.time.Duration

@ConfigurationProperties(prefix = "evil.bot")
@Validated
data class BotProperties(
    val captchaKickTimeout: Duration = Duration.ofMinutes(1),
    val cleanupLeftChatsTimeout: Duration = Duration.ofDays(30)
)
