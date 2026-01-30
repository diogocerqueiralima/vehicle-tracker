package com.github.diogocerqueiralima.infrastructure.exceptions;

public class InvalidCertificateException extends RuntimeException {

    public InvalidCertificateException() {
        super("The certificate is invalid.");
    }

}
