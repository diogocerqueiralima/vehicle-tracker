package com.github.diogocerqueiralima.domain.model;

import com.github.diogocerqueiralima.utils.UUIDv7;

import java.time.Instant;
import java.util.UUID;

public class LocationSnapshot {

    private final UUID id;
    private final Vehicle vehicle;
    private final double latitude;
    private final double longitude;
    private final double altitude;
    private final double speed;
    private final double course;

    public LocationSnapshot(
            UUID id, Vehicle vehicle, double latitude, double longitude, double altitude, double speed, double course
    ) {
        this.id = id;
        this.vehicle = vehicle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.course = course;
    }

    public LocationSnapshot(
            Vehicle vehicle, Instant timestamp, double latitude, double longitude, double altitude,
            double speed, double course
    ) {
        this(
                UUIDv7.from(timestamp.toEpochMilli()),
                vehicle,
                latitude,
                longitude,
                altitude,
                speed,
                course
        );
    }

    public Instant getTimestamp() {
        return Instant.ofEpochMilli(UUIDv7.extractTimestamp(this.id));
    }

    public UUID getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getSpeed() {
        return speed;
    }

    public double getCourse() {
        return course;
    }

}
