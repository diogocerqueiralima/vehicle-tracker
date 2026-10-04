package com.github.diogocerqueiralima.error.common.exceptions

/**
 * Exception thrown when a user does not have permission to access a resource or perform an action.
 */
open class ForbiddenException @JvmOverloads constructor(
    message: String,
    throwable: Throwable? = null
) : BaseException(message, throwable)
