package com.github.djaler.evilbot.model

import javax.validation.constraints.NotEmpty

class Reaction {
    @NotEmpty
    lateinit var reactions: List<String>

    var triggers: List<String> = emptyList()

    var chance = 100f

    var replyToBot = false
}
