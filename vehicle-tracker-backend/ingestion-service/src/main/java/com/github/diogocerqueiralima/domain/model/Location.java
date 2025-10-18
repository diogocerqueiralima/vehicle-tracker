package com.github.diogocerqueiralima.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Location(
        Instant timestamp, double latitude, double longitude, double altitude,
        double speed, double course, UUID deviceId
) {}