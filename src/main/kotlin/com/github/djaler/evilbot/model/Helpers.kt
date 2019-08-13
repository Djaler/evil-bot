package com.github.djaler.evilbot.model

data class GetOrCreateResult<T : Any>(val value: T, val created: Boolean)
