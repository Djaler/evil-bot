package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.time.Duration

@ConfigurationProperties(prefix = "cache")
@ConstructorBinding
@Validated
data class CacheProperties(
    val durations: Map<String, Duration>
)
