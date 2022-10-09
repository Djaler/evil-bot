package com.github.djaler.evilbot.config.vkcloud

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "vk.api")
@ConstructorBinding
data class VKCloudApiProperties(
    val key: String = ""
)
