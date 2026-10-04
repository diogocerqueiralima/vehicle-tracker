package com.github.diogocerqueiralima.error.common.exceptions

/**
 * Exception thrown when an operation fails due to an unexpected error or condition.
 */
open class OperationFailedException @JvmOverloads constructor(
    message: String,
    throwable: Throwable? = null
) : BaseException(message, throwable)
