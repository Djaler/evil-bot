package com.github.djaler.evilbot.config.fixer

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConfigurationProperties(prefix = "fixer.api")
@ConstructorBinding
data class FixerApiProperties(
    val key: String = ""
)
