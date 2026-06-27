package com.github.diogocerqueiralima.error.common.exceptions.exceptions;

/**
 * Exception thrown when a conflict occurs, such as when trying to create a resource that already exists.
 */
public class ConflictException extends BaseException {


    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
