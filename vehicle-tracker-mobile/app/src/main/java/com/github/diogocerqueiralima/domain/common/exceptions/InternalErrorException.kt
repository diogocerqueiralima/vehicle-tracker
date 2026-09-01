package com.github.diogocerqueiralima.domain.common.exceptions

/**
 * Thrown when an internal error occurs in the application.
 */
class InternalErrorException(
    override val message: String = "An internal error occurred. Please try again later."
) : Exception(message)