package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record LocationCommand(
        @NotNull Instant timestamp, @NotNull Double latitude, @NotNull Double longitude, @NotNull Double altitude,
        @NotNull Double speed, @NotNull Double course, @NotNull UUID deviceId
) {}
