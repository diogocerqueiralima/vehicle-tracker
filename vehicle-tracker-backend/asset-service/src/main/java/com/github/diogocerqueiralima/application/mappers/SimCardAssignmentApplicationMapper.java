package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.domain.assets.SimCard;
import com.github.diogocerqueiralima.domain.assignments.SimCardAssignment;
import com.github.diogocerqueiralima.domain.assets.Device;

import java.time.Instant;

/**
 * Mapper for SIM card assignment conversions in the application layer.
 */
public final class SimCardAssignmentApplicationMapper {

    // Should not be instantiated
    private SimCardAssignmentApplicationMapper() {}

    /**
     *
     * Builds a domain SIM card assignment from the command payload and resolved assets.
     *
     * @param command assignment command payload.
     * @param device resolved device domain object.
     * @param simCard resolved SIM card domain object.
     * @param assignedAt timestamp when assignment is created.
     * @return domain SIM card assignment ready for persistence.
     */
    public static SimCardAssignment toDomain(
            AssignDeviceToSimCardCommand command,
            Device device,
            SimCard simCard,
            Instant assignedAt
    ) {
        return new SimCardAssignment(
                device,
                simCard,
                assignedAt,
                null,
                command.assignedBy(),
                null,
                null
        );
    }

    /**
     *
     * Builds an application result from a domain SIM card assignment.
     *
     * @param simCardAssignment domain SIM card assignment.
     * @return assignment result contract.
     */
    public static SimCardAssignmentResult toResult(SimCardAssignment simCardAssignment) {
        return new SimCardAssignmentResult(
                simCardAssignment.getDevice().getId(),
                simCardAssignment.getSimCard().getId(),
                simCardAssignment.getAssignedAt(),
                simCardAssignment.getAssignedBy(),
                simCardAssignment.getUnassignedAt(),
                simCardAssignment.getUnassignedBy(),
                simCardAssignment.getRemovalReason(),
                simCardAssignment.isActive()
        );
    }

    /**
     *
     * Builds an updated domain SIM card assignment with unassignment data.
     *
     * @param command unassignment command payload.
     * @param activeAssignment currently active assignment.
     * @param unassignedAt timestamp when assignment is being closed.
     * @return domain assignment ready to be persisted as inactive.
     */
    public static SimCardAssignment toDomain(
            UnassignDeviceFromSimCardCommand command,
            SimCardAssignment activeAssignment,
            Instant unassignedAt
    ) {
        return new SimCardAssignment(
                activeAssignment.getId(),
                activeAssignment.getDevice(),
                activeAssignment.getSimCard(),
                activeAssignment.getAssignedAt(),
                unassignedAt,
                activeAssignment.getAssignedBy(),
                command.unassignedBy(),
                command.removalReason()
        );
    }

}

