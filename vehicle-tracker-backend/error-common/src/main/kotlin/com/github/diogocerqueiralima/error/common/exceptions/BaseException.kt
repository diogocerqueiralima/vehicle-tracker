package com.github.diogocerqueiralima.error.common.exceptions

/**
 * Base exception class for the application. All custom exceptions should extend this class.
 *
 * @param message The exception message.
 * @param throwable The cause of the exception.
 */
open class BaseException @JvmOverloads constructor(
    message: String,
    throwable: Throwable? = null
) : RuntimeException(message, throwable)
