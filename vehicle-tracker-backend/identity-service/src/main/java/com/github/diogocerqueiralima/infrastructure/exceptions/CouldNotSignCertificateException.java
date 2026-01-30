package com.github.diogocerqueiralima.infrastructure.exceptions;

public class CouldNotSignCertificateException extends RuntimeException {

    public CouldNotSignCertificateException() {
        super("Could not sign the certificate.");
    }

}
