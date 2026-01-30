package com.github.diogocerqueiralima.application.exceptions;

public class CertificateCouldNotBeParsedException extends RuntimeException {

    public CertificateCouldNotBeParsedException() {
        super("The certificate could not be parsed.");
    }

}
