package com.github.diogocerqueiralima.infrastructure.exceptions;

public class CouldNotConvertCertificateToPEMException extends RuntimeException {

    public CouldNotConvertCertificateToPEMException() {
        super("It was not possible to convert the certificate to PEM format.");
    }

}
