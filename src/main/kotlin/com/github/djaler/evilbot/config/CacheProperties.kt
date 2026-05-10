package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.time.Duration

@ConfigurationProperties(prefix = "cache")
@Validated
data class CacheProperties(
    val durations: Map<String, Duration>
)
