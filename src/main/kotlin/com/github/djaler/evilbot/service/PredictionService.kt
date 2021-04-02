package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.clients.SberClient
import org.springframework.stereotype.Service

@Service
class PredictionService(
    private val sberClient: SberClient
) {
    suspend fun getPrediction(text: String, leaveSource: Boolean = true): String {
        val prediction = sberClient.getPrediction(text)

        return if (leaveSource) prediction else prediction.removePrefix(text)
    }
}