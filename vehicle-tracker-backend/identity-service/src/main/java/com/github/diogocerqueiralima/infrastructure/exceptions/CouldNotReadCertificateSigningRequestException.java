package com.github.diogocerqueiralima.infrastructure.exceptions;

/**
 * Exception thrown when a Certificate Signing Request (CSR) could not be read.
 */
public class CouldNotReadCertificateSigningRequestException extends RuntimeException {

    /**
     * Constructs a new {@link CouldNotReadCertificateSigningRequestException} with a default message indicating that the CSR could not be read.
     */
    public CouldNotReadCertificateSigningRequestException() {
        super("Could not read the Certificate Signing Request (CSR).");
    }

}
