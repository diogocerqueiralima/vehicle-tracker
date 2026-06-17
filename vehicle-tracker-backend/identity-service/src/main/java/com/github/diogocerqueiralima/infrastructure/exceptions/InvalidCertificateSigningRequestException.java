package com.github.diogocerqueiralima.infrastructure.exceptions;

/**
 * Exception thrown when a certificate signing request is invalid.
 */
public class InvalidCertificateSigningRequestException extends RuntimeException {

    /**
     * Constructs a new {@link InvalidCertificateSigningRequestException} with a default message indicating that the certificate signing request is invalid.
     */
    public InvalidCertificateSigningRequestException() {
        super("The certificate signing request is invalid.");
    }

}
