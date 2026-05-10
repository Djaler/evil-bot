package com.github.djaler.evilbot.config.yandex

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "yandex.api")
data class YandexApiProperties(
    val token: String = "",
    val cookie: String = "",
)
