package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

import javax.validation.constraints.NotBlank


@ConfigurationProperties(prefix = "locationiq.api")
@ConstructorBinding
@Validated
data class LocationiqApiProperties(
    val key: String
)

