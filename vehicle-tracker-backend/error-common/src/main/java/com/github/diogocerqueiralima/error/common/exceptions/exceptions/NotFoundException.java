package com.github.diogocerqueiralima.error.common.exceptions.exceptions;

/**
 * Exception thrown when a requested resource is not found.
 */
public class NotFoundException extends BaseException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
