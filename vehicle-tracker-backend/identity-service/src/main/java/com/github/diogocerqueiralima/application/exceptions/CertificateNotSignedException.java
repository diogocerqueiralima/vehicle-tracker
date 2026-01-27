package com.github.diogocerqueiralima.application.exceptions;

public class CertificateNotSignedException extends RuntimeException {

    public CertificateNotSignedException() {
        super("The certificate could not be signed.");
    }

}
