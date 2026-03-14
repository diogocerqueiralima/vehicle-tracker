package com.github.diogocerqueiralima.domain.model.assignments;

import com.github.diogocerqueiralima.domain.model.assets.SimCard;
import com.github.diogocerqueiralima.domain.model.assets.Device;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents the assignment of a SIM card to a device.
 * This class extends the base {@link Assignment} class and includes specific details related to SIM card assignments.
 */
public class SimCardAssignment extends Assignment {

    private final Device device;
    private final SimCard simCard;
    private SimCardRemovalReason removalReason;

    public SimCardAssignment(Device device, SimCard simCard, Instant assignedAt, Instant unassignedAt, UUID unassignedBy, UUID assignedBy) {
        super(assignedAt, unassignedAt, assignedBy, unassignedBy, true);
        this.device = device;
        this.simCard = simCard;
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
