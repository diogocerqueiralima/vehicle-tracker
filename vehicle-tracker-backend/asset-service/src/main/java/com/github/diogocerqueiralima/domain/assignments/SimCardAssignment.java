package com.github.diogocerqueiralima.domain.assignments;

import com.github.diogocerqueiralima.domain.SimCard;
import com.github.diogocerqueiralima.domain.assets.Device;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the assignment of a SIM card to a device.
 * This class extends the base {@link Assignment} class and includes specific details related to SIM card assignments.
 */
public class SimCardAssignment extends Assignment {

    private final Device device;
    private final SimCard simCard;
    private final SimCardRemovalReason removalReason;

    public SimCardAssignment(
            Device device, SimCard simCard, Instant assignedAt, Instant unassignedAt, UUID assignedBy, UUID unassignedBy,
            SimCardRemovalReason removalReason
    ) {
        super(assignedAt, unassignedAt, assignedBy, unassignedBy);
        this.device = Objects.requireNonNull(device, "device cannot be null");
        this.simCard = Objects.requireNonNull(simCard, "simCard cannot be null");
        this.removalReason = removalReason;
    }

    public Device getDevice() {
        return device;
    }

    public SimCard getSimCard() {
        return simCard;
    }

    public SimCardRemovalReason getRemovalReason() {
        return removalReason;
    }

}
