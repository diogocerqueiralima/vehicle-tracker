package com.github.diogocerqueiralima.ingestion.service.application.usecases

import com.github.diogocerqueiralima.ingestion.service.application.commands.ReceiveLocationCommand
import com.github.diogocerqueiralima.ingestion.service.domain.model.Location
import com.github.diogocerqueiralima.ingestion.service.domain.ports.inbound.LocationUseCase
import com.github.diogocerqueiralima.ingestion.service.domain.ports.outbound.LocationPublisher
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.floor

@Service
class LocationUseCaseImpl(private val locationPublisher: LocationPublisher) : LocationUseCase {

    override fun receive(command: ReceiveLocationCommand) {

        val location = Location(
            timestamp = nmeaToInstant(command.date, command.time),
            latitude = nmeaCoordinateToDecimal(command.latitude, command.latitudeDirection),
            longitude = nmeaCoordinateToDecimal(command.longitude, command.longitudeDirection),
            altitude = command.altitude,
            speed = command.speed * (1852.0 / 3600000.0), // Convert from knots to m/s
            course = command.course,
            deviceId = command.deviceId
        )

        locationPublisher.publish(location)
    }

    /**
     * Converts NMEA date and time values to an [Instant].
     *
     * @param date the date in NMEA format (DDMMYY)
     * @param time the time in NMEA format (HHMMSS[.sss])
     * @return the corresponding [Instant]
     */
    private fun nmeaToInstant(date: Int, time: Double): Instant {

        val hour = (time / 10000).toInt()
        val minute = ((time % 10000) / 100).toInt()
        val second = (time % 100).toInt()

        val nano = ((time - floor(time)) * 1_000_000_000).toInt()

        val day = date / 10000
        val month = (date % 10000) / 100
        val year = (date % 100) + 2000

        return LocalDateTime.of(year, month, day, hour, minute, second, nano).toInstant(ZoneOffset.UTC)
    }

    /**
     * Converts NMEA coordinate format to decimal degrees.
     *
     * @param coordinate the coordinate in NMEA format (DDMM.MMMM or DDDMM.MMMM)
     * @param direction the direction character ('N', 'S', 'E', 'W')
     * @return the coordinate in decimal degrees
     */
    private fun nmeaCoordinateToDecimal(coordinate: String, direction: String): Double {

        // 1. Latitudes carry 2 degree digits, longitudes carry 3
        val degreeLength = if (direction == "N" || direction == "S") 2 else 3

        val degrees = coordinate.substring(0, degreeLength).toDouble()
        val minutes = coordinate.substring(degreeLength).toDouble()

        val decimalDegrees = degrees + (minutes / 60.0)

        // 2. South and west are negative
        return if (direction == "S" || direction == "W") -decimalDegrees else decimalDegrees
    }

}
