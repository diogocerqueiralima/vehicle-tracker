package com.github.diogocerqueiralima.infrastructure.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "location_snapshots")
public class LocationSnapshotEntity {

    @Id
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private double altitude;

    @Column(nullable = false)
    private double speed;

    @Column(nullable = false)
    private double course;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setCourse(double course) {
        this.course = course;
    }

}
