package com.github.diogocerqueiralima.infrastructure.exceptions;

/**
 * Exception thrown when a certificate could not be parsed.
 */
public class CertificateCouldNotBeParsedException extends RuntimeException {

    /**
     * Constructs a new {@link CertificateCouldNotBeParsedException} with a default message indicating that the certificate could not be parsed.
     */
    public CertificateCouldNotBeParsedException() {
        super("The certificate could not be parsed.");
    }

}
