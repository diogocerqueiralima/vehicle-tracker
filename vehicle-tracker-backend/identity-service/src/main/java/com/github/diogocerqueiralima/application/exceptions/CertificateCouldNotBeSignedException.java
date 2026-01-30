package com.github.diogocerqueiralima.application.exceptions;

public class CertificateCouldNotBeSignedException extends RuntimeException {

    public CertificateCouldNotBeSignedException() {
        super("The certificate could not be signed.");
    }

}
