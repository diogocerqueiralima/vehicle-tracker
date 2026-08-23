package com.github.diogocerqueiralima.domain.exceptions

import java.util.UUID

/**
 * Thrown when a Device with the given identifier does not exist.
 */
class DeviceNotFoundException(id: UUID) : Exception("Device not found with id: $id")
