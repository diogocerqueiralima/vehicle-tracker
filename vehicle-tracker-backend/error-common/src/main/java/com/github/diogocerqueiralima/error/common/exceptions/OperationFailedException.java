package com.github.diogocerqueiralima.error.common.exceptions;

/**
 * Exception thrown when an operation fails due to an unexpected error or condition.
 */
public class OperationFailedException extends BaseException {

    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
