package com.github.djaler.evilbot.config.vkcloud

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "vk.api")
data class VKCloudApiProperties(
    val key: String = ""
)
