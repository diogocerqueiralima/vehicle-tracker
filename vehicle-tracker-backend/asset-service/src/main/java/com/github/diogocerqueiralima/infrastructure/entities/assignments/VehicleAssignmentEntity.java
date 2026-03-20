package com.github.diogocerqueiralima.infrastructure.entities.assignments;

import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "vehicle_assignments")
@PrimaryKeyJoinColumn(name = "id")
public class VehicleAssignmentEntity extends AssignmentEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceEntity device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "removal_reason", length = 32)
    private VehicleRemovalReason removalReason;

    @Column(name = "installed_by")
    private UUID installedBy;

    @Column(name = "notes", length = 1024)
    private String notes;

    public VehicleAssignmentEntity() {}

    public DeviceEntity getDevice() {
        return device;
    }

    public void setDevice(DeviceEntity device) {
        this.device = device;
    }

    public VehicleEntity getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleEntity vehicle) {
        this.vehicle = vehicle;
    }

    public VehicleRemovalReason getRemovalReason() {
        return removalReason;
    }

    public void setRemovalReason(VehicleRemovalReason removalReason) {
        this.removalReason = removalReason;
    }

    public UUID getInstalledBy() {
        return installedBy;
    }

    public void setInstalledBy(UUID installedBy) {
        this.installedBy = installedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}

