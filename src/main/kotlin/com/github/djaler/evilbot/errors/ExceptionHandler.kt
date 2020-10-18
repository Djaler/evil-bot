package com.github.djaler.evilbot.errors

interface ExceptionHandler {
    suspend fun handleException(e: Exception)
}
