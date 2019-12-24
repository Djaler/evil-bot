package com.github.djaler.evilbot.components

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Component
class CasClient(
    private val casRestTemplate: RestTemplate
) {
    fun getCasInfo(userId: Int): CasInfo {
        return casRestTemplate.getForObject("https://api.cas.chat/check?user_id=$userId")
    }
}

data class CasInfo(
    val result: CasResult?
)

data class CasResult(
    val messages: List<String>
)
