package com.github.djaler.evilbot.model

import javax.validation.constraints.NotEmpty

data class Reaction(
    @field:NotEmpty
    val reactions: List<String>,
    val triggers: List<String> = emptyList(),
    val chance: Float = 100f,
    val replyToBot: Boolean = false
)
