package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.time.Duration

@ConfigurationProperties(prefix = "evil.bot")
@ConstructorBinding
@Validated
data class BotProperties(
    val captchaKickTimeout: Duration = Duration.ofMinutes(1)
)
