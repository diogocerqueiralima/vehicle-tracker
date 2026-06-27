package com.github.diogocerqueiralima.error.common.exceptions;

/**
 * Base exception class for the application. All custom exceptions should extend this class.
 */
public class BaseException extends RuntimeException {

    /**
     * @param message The exception message.
     */
    public BaseException(String message) {
        super(message);
    }

    /**
     * @param message The exception message.
     * @param throwable The cause of the exception.
     */
    public BaseException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
