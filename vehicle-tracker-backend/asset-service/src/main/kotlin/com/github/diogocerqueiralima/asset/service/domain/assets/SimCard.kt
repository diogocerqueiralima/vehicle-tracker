package com.github.diogocerqueiralima.asset.service.domain.assets

import java.time.Instant
import java.util.UUID

/**
 * Represents a SIM card in the system.
 */
class SimCard(
    id: UUID,
    ownerId: UUID?,
    createdAt: Instant,
    updatedAt: Instant,
    val iccid: String,
    val msisdn: String,
    val imsi: String
) : Asset(id, ownerId, createdAt, updatedAt) {

    constructor(
        id: UUID, createdAt: Instant, updatedAt: Instant, iccid: String, msisdn: String, imsi: String
    ) : this(id, null, createdAt, updatedAt, iccid, msisdn, imsi)

}
