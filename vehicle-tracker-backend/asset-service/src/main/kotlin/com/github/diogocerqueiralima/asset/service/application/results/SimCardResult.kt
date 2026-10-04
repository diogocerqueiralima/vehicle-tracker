package com.github.diogocerqueiralima.asset.service.application.results

import java.time.Instant
import java.util.UUID

/**
 * Result returned by SIM card application use cases.
 */
data class SimCardResult(
    val id: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
    val iccid: String,
    val msisdn: String,
    val imsi: String
)
