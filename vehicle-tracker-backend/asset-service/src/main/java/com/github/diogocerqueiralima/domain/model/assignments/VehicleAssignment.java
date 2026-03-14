package com.github.diogocerqueiralima.domain.model.assignments;

import com.github.diogocerqueiralima.domain.model.assets.Device;
import com.github.diogocerqueiralima.domain.model.assets.Vehicle;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the assignment of a device to a vehicle.
 * This class extends the base {@link Assignment} class and includes specific details related to vehicle assignments.
 */
public class VehicleAssignment extends Assignment {

    private final Device device;
    private final Vehicle vehicle;
    private final VehicleRemovalReason removalReason;
    private final UUID installedBy;
    private final String notes;

    public VehicleAssignment(
            Device device, Vehicle vehicle, Instant assignedAt, Instant unassignedAt, UUID assignedBy, UUID unassignedBy,
            VehicleRemovalReason removalReason, UUID installedBy, String notes
    ) {
        super(assignedAt, unassignedAt, assignedBy, unassignedBy);
        this.device = Objects.requireNonNull(device, "device cannot be null");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle cannot be null");
        this.removalReason = removalReason;
        this.installedBy = installedBy;
        this.notes = notes;
    }

    public Device getDevice() {
        return device;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public VehicleRemovalReason getRemovalReason() {
        return removalReason;
    }

    public UUID getInstalledBy() {
        return installedBy;
    }

    public String getNotes() {
        return notes;
    }

}
