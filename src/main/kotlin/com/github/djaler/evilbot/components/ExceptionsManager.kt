package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.errors.ExceptionHandler
import org.springframework.stereotype.Component

@Component
class ExceptionsManager(
    private val exceptionHandlers: List<ExceptionHandler>
) {
    suspend fun process(e: Exception) {
        for (exceptionHandler in exceptionHandlers) {
            exceptionHandler.handleException(e)
        }
    }
}
