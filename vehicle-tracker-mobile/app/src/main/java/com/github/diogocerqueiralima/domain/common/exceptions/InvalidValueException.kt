package com.github.diogocerqueiralima.domain.common.exceptions

/**
 * Thrown when the device refuses a written value as invalid for the characteristic (BLE ATT
 * "Value Not Allowed"), as opposed to a transport/connection failure.
 */
class InvalidValueException(
    override val message: String = "The device rejected the written value."
) : Exception(message)
