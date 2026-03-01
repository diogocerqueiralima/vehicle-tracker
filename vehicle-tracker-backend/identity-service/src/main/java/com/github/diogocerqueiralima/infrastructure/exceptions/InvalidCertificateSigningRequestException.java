package com.github.diogocerqueiralima.infrastructure.exceptions;

public class InvalidCertificateSigningRequestException extends RuntimeException {

    public InvalidCertificateSigningRequestException() {
        super("The certificate signing request is invalid.");
    }

}
