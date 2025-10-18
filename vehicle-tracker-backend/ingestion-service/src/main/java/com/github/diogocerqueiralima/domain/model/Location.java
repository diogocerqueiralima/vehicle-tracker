package com.github.diogocerqueiralima.domain.model;

import java.time.Instant;

public record Location(Instant timestamp, double latitude, double longitude, double altitude) {}