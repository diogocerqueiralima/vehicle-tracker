package com.github.diogocerqueiralima.domain.assets;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for all assets in the system.
 * An asset can be a {@link Vehicle}, a {@link Device}.
 */
public abstract class Asset {

    private final UUID id;
    private final Instant createdAt;
    private final Instant updatedAt;

    protected Asset(UUID id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
