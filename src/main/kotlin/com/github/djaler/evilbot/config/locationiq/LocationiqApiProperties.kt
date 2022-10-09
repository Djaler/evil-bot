package com.github.djaler.evilbot.config.locationiq

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConfigurationProperties(prefix = "locationiq.api")
@ConstructorBinding
data class LocationiqApiProperties(
    val key: String = ""
)
