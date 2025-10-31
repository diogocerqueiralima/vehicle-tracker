package com.github.diogocerqueiralima.domain.model;

import java.util.UUID;

public class LocationSnapshot {

    private final UUID vehicleId;
    private final double latitude;
    private final double longitude;
    private final double altitude;
    private final long timestamp;
    private final double speed;
    private final double course;

    public LocationSnapshot(
            UUID vehicleId, double latitude, double longitude, double altitude,
            long timestamp, double speed, double course
    ) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timestamp = timestamp;
        this.speed = speed;
        this.course = course;
    }

}
