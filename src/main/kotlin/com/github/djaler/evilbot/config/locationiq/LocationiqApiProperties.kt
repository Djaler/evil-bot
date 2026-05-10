package com.github.djaler.evilbot.config.locationiq

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "locationiq.api")
data class LocationiqApiProperties(
    val key: String = ""
)
