package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@ConfigurationProperties(prefix = "telegram.bot")
@Validated
class TelegramProperties {
    lateinit var token: String
    val webhook: WebhookProperties = WebhookProperties()

    class WebhookProperties {
        var url: String? = null

        @Min(0)
        @Max(65535)
        var port: Int = 8080
    }
}
