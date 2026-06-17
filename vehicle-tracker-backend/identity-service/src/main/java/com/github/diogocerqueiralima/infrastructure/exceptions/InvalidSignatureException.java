package com.github.diogocerqueiralima.infrastructure.exceptions;

/**
 * Exception thrown when a signature is invalid.
 */
public class InvalidSignatureException extends RuntimeException {

    /**
     * Constructs a new {@link InvalidSignatureException} with a default message indicating that the signature is invalid.
     */
    public InvalidSignatureException() {
        super("The signature is invalid.");
    }

}
