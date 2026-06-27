package com.github.diogocerqueiralima.error.common.exceptions.exceptions;

/**
 * Exception thrown when a user does not have permission to access a resource or perform an action.
 */
public class ForbiddenException extends BaseException {


    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
