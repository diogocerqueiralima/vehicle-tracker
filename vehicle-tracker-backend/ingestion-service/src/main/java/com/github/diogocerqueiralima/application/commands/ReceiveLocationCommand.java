package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 *
 * Command to receive location data.
 * All the adapters must provide these data.
 * The data should be in NMEA format.
 *
 * @param time      in format HHMMSS
 * @param date      in format DDMMYY
 * @param latitude  in format D°MM.MMMM
 * @param longitude in format D°MM.MMMM
 * @param altitude  in meters
 * @param speed     in knots
 * @param course    in degrees
 * @param deviceId  unique identifier of the device
 */
public record ReceiveLocationCommand(
        @NotBlank String time,
        @NotBlank String date,
        @NotBlank String latitude,
        @NotBlank @Size(min = 1, max = 1) String latitudeDirection,
        @NotBlank String longitude,
        @NotBlank @Size(min = 1, max = 1) String longitudeDirection,
        @NotNull double altitude,
        @NotNull double speed,
        @NotNull double course,
        @NotNull UUID deviceId
) {}
