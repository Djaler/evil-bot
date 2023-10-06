package com.github.djaler.evilbot.config.yandex

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "yandex.api")
@ConstructorBinding
data class YandexApiProperties(
    val token: String = ""
)
