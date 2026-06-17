package com.github.diogocerqueiralima.infrastructure.exceptions;

/**
 * Exception thrown when a common name is invalid.
 */
public class InvalidCommonNameException extends RuntimeException {

    /**
     * Constructs a new {@link InvalidCommonNameException} with a default message indicating that the common name is invalid.
     */
    public InvalidCommonNameException() {
        super("The common name is invalid.");
    }

}
