package com.github.diogocerqueiralima.error.common.exceptions

/**
 * Exception thrown when a request is invalid or cannot be processed due to client-side errors.
 */
open class BadRequestException @JvmOverloads constructor(
    message: String,
    throwable: Throwable? = null
) : BaseException(message, throwable)
