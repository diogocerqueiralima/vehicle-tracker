package com.github.diogocerqueiralima.ingestion.service.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Location(
        Instant timestamp, double latitude, double longitude, double altitude,
        double speed, double course, UUID deviceId
) {}