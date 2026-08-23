package com.github.diogocerqueiralima.domain.exceptions

import java.util.UUID

/**
 * Thrown when a Vehicle with the given identifier does not exist.
 */
class VehicleNotFoundException(id: UUID) : Exception("Vehicle not found with id: $id")
