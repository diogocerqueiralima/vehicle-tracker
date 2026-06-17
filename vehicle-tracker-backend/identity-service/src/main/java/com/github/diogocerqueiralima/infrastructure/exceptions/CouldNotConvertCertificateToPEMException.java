package com.github.diogocerqueiralima.infrastructure.exceptions;

/**
 * Exception thrown when a certificate could not be converted to PEM format.
 */
public class CouldNotConvertCertificateToPEMException extends RuntimeException {

    /**
     * Constructs a new {@link CouldNotConvertCertificateToPEMException} with a default message indicating that the certificate could not be converted to PEM format.
     */
    public CouldNotConvertCertificateToPEMException() {
        super("It was not possible to convert the certificate to PEM format.");
    }

}
