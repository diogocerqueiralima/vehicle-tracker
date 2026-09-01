package com.github.diogocerqueiralima.domain.common.exceptions

import java.util.UUID

/**
 * Thrown when a resource with the given identifier does not exist.
 */
class NotFoundException(id: UUID) : Exception("Resource not found with id: $id")
