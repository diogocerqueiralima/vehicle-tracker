package com.github.diogocerqueiralima.ingestion.service.application.commands

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Command to receive location data.
 * All the adapters must provide these data.
 * The data should be in NMEA format.
 *
 * @property time in format HHMMSS
 * @property date in format DDMMYY
 * @property latitude in format D°MM.MMMM
 * @property longitude in format D°MM.MMMM
 * @property altitude in meters
 * @property speed in knots
 * @property course in degrees
 * @property deviceId unique identifier of the device
 */
data class ReceiveLocationCommand(
    @get:NotNull val time: Double,
    @get:NotNull val date: Int,
    @get:NotBlank val latitude: String,
    @get:NotBlank @get:Size(min = 1, max = 1) val latitudeDirection: String,
    @get:NotBlank val longitude: String,
    @get:NotBlank @get:Size(min = 1, max = 1) val longitudeDirection: String,
    @get:NotNull val altitude: Double,
    @get:NotNull val speed: Double,
    @get:NotNull val course: Double,
    @get:NotNull val deviceId: UUID
)
