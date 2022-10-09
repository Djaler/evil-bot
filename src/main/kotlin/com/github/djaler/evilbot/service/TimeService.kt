package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.clients.LocationiqClient
import com.github.djaler.evilbot.config.locationiq.LocationiqApiCondition
import io.ktor.client.plugins.*
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
@Conditional(LocationiqApiCondition::class)
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

        return getTimeForLocation(location.lat, location.lon)
    }

    suspend fun getTimeForLocation(latitude: Double, longitude: Double): LocalDateTime {
        val locationTimezone = locationiqClient.getTimezone(latitude, longitude)

        return LocalDateTime.now(ZoneOffset.UTC).plusSeconds(locationTimezone.offsetSec)
    }
}
