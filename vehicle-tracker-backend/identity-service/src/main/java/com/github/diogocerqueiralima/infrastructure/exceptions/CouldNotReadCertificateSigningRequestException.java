package com.github.diogocerqueiralima.infrastructure.exceptions;

public class CouldNotReadCertificateSigningRequestException extends RuntimeException {

    public CouldNotReadCertificateSigningRequestException() {
        super("Could not read the Certificate Signing Request (CSR).");
    }

}
