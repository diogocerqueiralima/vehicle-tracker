package com.github.diogocerqueiralima.identity.service.domain.model.device

import java.util.UUID

/**
 * Represents a device in the system.
 *
 * @property id the unique identifier of the device
 * @property ownerId the unique identifier of the owner of the device
 */
class Device(val id: UUID, val ownerId: UUID)
