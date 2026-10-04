package com.github.diogocerqueiralima.error.common.exceptions

/**
 * Exception thrown when a conflict occurs, such as when trying to create a resource that already exists.
 */
open class ConflictException @JvmOverloads constructor(
    message: String,
    throwable: Throwable? = null
) : BaseException(message, throwable)
