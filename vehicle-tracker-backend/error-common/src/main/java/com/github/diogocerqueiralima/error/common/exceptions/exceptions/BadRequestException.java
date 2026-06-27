package com.github.diogocerqueiralima.error.common.exceptions.exceptions;

/**
 * Exception thrown when a request is invalid or cannot be processed due to client-side errors.
 */
public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
