package com.github.diogocerqueiralima.infrastructure.exceptions;

/**
 * Exception thrown when a certificate could not be signed.
 */
public class CouldNotSignCertificateException extends RuntimeException {

    /**
     * Constructs a new {@link CouldNotSignCertificateException} with a default message indicating that the certificate could not be signed.
     */
    public CouldNotSignCertificateException() {
        super("Could not sign the certificate.");
    }

}
