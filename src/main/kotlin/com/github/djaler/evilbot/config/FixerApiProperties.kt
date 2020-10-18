package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

import javax.validation.constraints.NotBlank


@ConfigurationProperties(prefix = "fixer.api")
@ConstructorBinding
@Validated
data class FixerApiProperties(
    @field:NotBlank
    val key: String
)

