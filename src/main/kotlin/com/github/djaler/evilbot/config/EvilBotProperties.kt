package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "evil.bot")
@Component
@Validated
class EvilBotProperties {
    var captchaEnabled = false
}
