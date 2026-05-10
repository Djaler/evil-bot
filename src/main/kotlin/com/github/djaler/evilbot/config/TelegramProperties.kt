package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@ConfigurationProperties(prefix = "telegram.bot")
@Validated
data class TelegramProperties(
    val token: String,
    val webhook: WebhookProperties = WebhookProperties()
)

data class WebhookProperties(
    val url: String? = null,
    @field:Min(0)
    @field:Max(65535)
    val port: Int = 8080
)
