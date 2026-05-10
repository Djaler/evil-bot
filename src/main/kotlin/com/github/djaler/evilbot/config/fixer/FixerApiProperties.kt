package com.github.djaler.evilbot.config.fixer

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "fixer.api")
data class FixerApiProperties(
    val key: String = ""
)
