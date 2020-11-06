package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.clients.LocationiqClient
import io.ktor.client.features.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class TimeService(
    private val locationiqClient: LocationiqClient
) {
    suspend fun getTimeForLocation(locationQuery: String): LocalDateTime? {
        val locations = try {
            locationiqClient.getLocation(locationQuery)
        } catch (e: ClientRequestException) {
            return null
        }

        val location = locations.first()
        val locationTimezone = locationiqClient.getTimezone(location.lat, location.lon)

        return LocalDateTime.now(ZoneOffset.UTC).plusSeconds(locationTimezone.offsetSec)
    }
}
