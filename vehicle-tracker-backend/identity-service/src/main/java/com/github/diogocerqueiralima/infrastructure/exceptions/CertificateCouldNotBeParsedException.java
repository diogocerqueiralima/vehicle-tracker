package com.github.diogocerqueiralima.infrastructure.exceptions;

public class CertificateCouldNotBeParsedException extends RuntimeException {

    public CertificateCouldNotBeParsedException() {
        super("The certificate could not be parsed.");
    }

}
