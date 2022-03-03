package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "vk.api")
@ConstructorBinding
@Validated
data class VKCloudApiProperties(
    val key: String
)
