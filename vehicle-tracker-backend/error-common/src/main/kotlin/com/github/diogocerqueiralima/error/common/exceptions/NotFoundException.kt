package com.github.diogocerqueiralima.error.common.exceptions

/**
 * Exception thrown when a requested resource is not found.
 */
open class NotFoundException @JvmOverloads constructor(
    message: String,
    throwable: Throwable? = null
) : BaseException(message, throwable)
