package com.github.diogocerqueiralima.infrastructure.exceptions;

/**
 * Exception thrown when a signature could not be verified.
 */
public class SignatureVerificationException extends RuntimeException {

    /**
     * Constructs a new {@link SignatureVerificationException} with a default message indicating that the signature could not be verified.
     */
    public SignatureVerificationException() {
        super("The signature could not be verified.");
    }

}
