package com.github.diogocerqueiralima.domain.common.exceptions

import java.util.UUID

/**
 * Thrown when a resource exists but refuses the operation in its current state, as opposed to
 * failing to carry it out.
 */
class BadRequestException(id: UUID) : Exception("Resource does not allow this operation in its current state: $id")
