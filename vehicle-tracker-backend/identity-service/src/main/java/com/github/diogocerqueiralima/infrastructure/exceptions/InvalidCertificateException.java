package com.github.diogocerqueiralima.infrastructure.exceptions;

/**
 * Exception thrown when a certificate is invalid.
 */
public class InvalidCertificateException extends RuntimeException {

    /**
     * Constructs a new {@link InvalidCertificateException} with a default message indicating that the certificate is invalid.
     */
    public InvalidCertificateException() {
        super("The certificate is invalid.");
    }

}
