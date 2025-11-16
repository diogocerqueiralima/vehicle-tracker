package com.github.diogocerqueiralima.infrastructure.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "locations_snapshots", schema = "vehicle_tracker_schema")
public class LocationSnapshotEntity {

    @Id
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point point;

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

    public void setPoint(Point point) {
        this.point = point;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setCourse(double course) {
        this.course = course;
    }

}
