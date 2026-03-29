package com.github.diogocerqueiralima.domain.assignments;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>Base class for all assignments in the system.
 * An assignment can be a {@link VehicleAssignment} or a {@link SimCardAssignment}.
 * <p>The {@link Assignment} is responsible for tracking the assignment history of assets,
 * including the timestamps of when the assignment was made and when it was removed,
 * as well as the users responsible for these actions.
 */
public abstract class Assignment {

    private final Long id;
    private final Instant assignedAt;
    private final Instant unassignedAt;
    private final UUID assignedBy;
    private final UUID unassignedBy;
    private final boolean active;

    protected Assignment(
            Instant assignedAt, Instant unassignedAt, UUID assignedBy, UUID unassignedBy
    ) {
        this(null, assignedAt, unassignedAt, assignedBy, unassignedBy);
    }

    protected Assignment(
            Long id,
            Instant assignedAt,
            Instant unassignedAt,
            UUID assignedBy,
            UUID unassignedBy
    ) {
        this.id = id;
        this.assignedAt = Objects.requireNonNull(assignedAt, "assignedAt cannot be null");
        this.assignedBy = Objects.requireNonNull(assignedBy, "assignedBy cannot be null");
        this.unassignedAt = unassignedAt;
        this.unassignedBy = unassignedBy;
        this.active = unassignedAt == null;
    }

    public Long getId() {
        return id;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getUnassignedAt() {
        return unassignedAt;
    }

    public UUID getAssignedBy() {
        return assignedBy;
    }

    public UUID getUnassignedBy() {
        return unassignedBy;
    }

    public boolean isActive() {
        return active;
    }

    /**
     *
     * Calculates the duration of the assignment.
     * If the assignment is still active, it calculates the duration from the assignedAt timestamp to the current time.
     * If the assignment has been unassigned, it calculates the duration from the assignedAt timestamp to the unassignedAt timestamp.
     *
     * @return the duration of the assignment
     */
    public Duration getDuration() {
        Instant end = active ? Instant.now() : unassignedAt;
        return Duration.between(assignedAt, end);
    }

}
