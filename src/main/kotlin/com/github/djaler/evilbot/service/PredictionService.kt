package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.clients.SberClient
import org.springframework.stereotype.Service

@Service
class PredictionService(
    private val sberClient: SberClient
) {
    class TooBigTextException : Exception()

    @Throws(TooBigTextException::class)
    suspend fun getPrediction(text: String, leaveSource: Boolean = true): String {
        val prediction = sberClient.getPrediction(text)

        if (prediction.length <= text.length) {
            throw TooBigTextException()
        }

        return if (leaveSource) prediction else prediction.removePrefix(text)
    }
}
