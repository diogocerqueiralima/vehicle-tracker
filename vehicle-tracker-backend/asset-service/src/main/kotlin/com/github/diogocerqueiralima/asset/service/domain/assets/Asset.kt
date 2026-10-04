package com.github.diogocerqueiralima.asset.service.domain.assets

import java.time.Instant
import java.util.UUID

/**
 * Base class for all assets in the system.
 * An asset can be a [Vehicle], a [Device].
 *
 * @property id unique identifier of the asset
 * @property ownerId identifier of the user that owns the asset, if any
 * @property createdAt when the asset was created
 * @property updatedAt when the asset was last updated
 */
abstract class Asset protected constructor(
    val id: UUID,
    val ownerId: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant
) {

    protected constructor(id: UUID, createdAt: Instant, updatedAt: Instant) : this(id, null, createdAt, updatedAt)

}
